package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.dto.RegisterRequest;
import com.erp.Enterprise.Resource.Planning.dto.RegisterResponse;
import com.erp.Enterprise.Resource.Planning.dto.ResendOtpRequest;
import com.erp.Enterprise.Resource.Planning.dto.VerifyEmailRequest;
import com.erp.Enterprise.Resource.Planning.entity.EmailVerificationOtp;
import com.erp.Enterprise.Resource.Planning.entity.Employee;
import com.erp.Enterprise.Resource.Planning.entity.Employment;
import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import com.erp.Enterprise.Resource.Planning.entity.Role;
import com.erp.Enterprise.Resource.Planning.entity.UserAccount;
import com.erp.Enterprise.Resource.Planning.entity.UserStatus;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import com.erp.Enterprise.Resource.Planning.repository.EmailVerificationOtpRepository;
import com.erp.Enterprise.Resource.Planning.repository.EmployeeRepository;
import com.erp.Enterprise.Resource.Planning.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private static final int OTP_BOUND = 1_000_000;
    private static final long OTP_EXPIRY_MINUTES = 10;

    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final EmailVerificationOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "An employee with this email already exists.");
        }
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(request.employeeCode())) {
            throw new ApiException(HttpStatus.CONFLICT, "An employee with this employee ID already exists.");
        }

        Employee employee = new Employee();
        employee.setEmployeeCode(request.employeeCode().trim());
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setDistrict(request.district());
        employee.setMobile(request.mobile());
        employee.setDateOfBirth(request.dateOfBirth());

        Employment employment = new Employment();
        employment.setInstitution(request.institution().trim());
        employment.setDepartment(request.department().trim());
        employment.setPosition(request.position().trim());
        employment.setBaseSalary(request.baseSalary());
        employment.setStatus(EmploymentStatus.ACTIVE);
        employment.setJoiningDate(request.joiningDate());

        UserAccount account = new UserAccount();
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setStatus(UserStatus.PENDING_VERIFICATION);
        account.setRoles(Set.of(Role.ROLE_EMPLOYEE));

        employee.assignEmployment(employment);
        employee.assignUserAccount(account);
        employeeRepository.saveAndFlush(employee);

        sendOtp(account);
        return new RegisterResponse(employee.getEmail(), "Registration successful. Check your email for the verification OTP.", OTP_EXPIRY_MINUTES);
    }

    @Transactional
    public RegisterResponse resendOtp(ResendOtpRequest request) {
        UserAccount account = userAccountRepository.findByEmployeeEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Registered email was not found."));
        if (account.getStatus() == UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "This email is already verified.");
        }

        sendOtp(account);
        return new RegisterResponse(account.getEmployee().getEmail(), "A new verification OTP has been sent.", OTP_EXPIRY_MINUTES);
    }

    @Transactional
    public RegisterResponse verifyEmail(VerifyEmailRequest request) {
        EmailVerificationOtp otp = otpRepository.findLatestPendingByEmail(request.email(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No pending OTP was found for this email."));

        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP has expired. Request a new OTP.");
        }
        if (!otp.getOtpCode().equals(request.otp())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP code.");
        }

        otp.setVerifiedAt(LocalDateTime.now());
        otp.getUserAccount().setStatus(UserStatus.ACTIVE);
        return new RegisterResponse(otp.getUserAccount().getEmployee().getEmail(), "Email verified successfully. You can now login.", 0);
    }

    private void sendOtp(UserAccount account) {
        EmailVerificationOtp otp = new EmailVerificationOtp();
        otp.setUserAccount(account);
        otp.setOtpCode(generateOtp());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpRepository.save(otp);

        String subject = "ERP Email Verification OTP";
        String body = """
                Dear %s,

                Your ERP email verification OTP is: %s

                This code expires in %d minutes.
                """.formatted(
                account.getEmployee().getFirstName(),
                otp.getOtpCode(),
                OTP_EXPIRY_MINUTES
        );

        emailNotificationService.sendOtpEmail(account.getEmployee().getEmail(), subject, body);
    }

    private String generateOtp() {
        return "%06d".formatted(secureRandom.nextInt(OTP_BOUND));
    }
}
