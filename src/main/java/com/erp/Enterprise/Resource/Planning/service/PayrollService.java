package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.dto.MessageResponse;
import com.erp.Enterprise.Resource.Planning.dto.EmailResponse;
import com.erp.Enterprise.Resource.Planning.dto.PayrollRequest;
import com.erp.Enterprise.Resource.Planning.dto.PayslipResponse;
import com.erp.Enterprise.Resource.Planning.entity.Employee;
import com.erp.Enterprise.Resource.Planning.entity.Employment;
import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import com.erp.Enterprise.Resource.Planning.entity.Message;
import com.erp.Enterprise.Resource.Planning.entity.Payslip;
import com.erp.Enterprise.Resource.Planning.entity.PayslipStatus;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import com.erp.Enterprise.Resource.Planning.repository.EmploymentRepository;
import com.erp.Enterprise.Resource.Planning.repository.MessageRepository;
import com.erp.Enterprise.Resource.Planning.repository.PayslipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final EmploymentRepository employmentRepository;
    private final PayslipRepository payslipRepository;
    private final MessageRepository messageRepository;
    private final DeductionService deductionService;
    private final CurrentUserService currentUserService;
    private final EmailNotificationService emailNotificationService;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<PayslipResponse> processPayroll(PayrollRequest request) {
        PayrollRates rates = loadRates();
        List<Employment> activeEmployments = employmentRepository.findByStatus(EmploymentStatus.ACTIVE);
        if (activeEmployments.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No active employees are available for payroll.");
        }

        List<Employment> duplicates = activeEmployments.stream()
                .filter(employment -> payslipRepository.existsByEmployeeIdAndMonthAndYear(
                        employment.getEmployee().getId(),
                        request.month(),
                        request.year()
                ))
                .toList();
        if (!duplicates.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Payroll already exists for at least one active employee in this month/year.");
        }

        List<Payslip> payslips = activeEmployments.stream()
                .map(employment -> computePayslip(employment, rates, request.month(), request.year()))
                .toList();

        return payslipRepository.saveAll(payslips).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<PayslipResponse> approvePayroll(PayrollRequest request) {
        List<Payslip> pendingPayslips = payslipRepository.findByMonthAndYearAndStatus(
                request.month(),
                request.year(),
                PayslipStatus.PENDING
        );
        if (pendingPayslips.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No pending payslips were found for this month/year.");
        }

        if (isPostgreSql()) {
            jdbcTemplate.queryForObject("select approve_payroll(?, ?)", Integer.class, request.month(), request.year());
            entityManager.clear();
        } else {
            pendingPayslips.forEach(this::markPaidAndCreateMessage);
        }

        List<Payslip> approvedPayslips = payslipRepository.findByMonthAndYear(request.month(), request.year())
                .stream()
                .filter(payslip -> payslip.getStatus() == PayslipStatus.PAID)
                .toList();
        sendPaymentApprovalEmails(approvedPayslips);

        return approvedPayslips.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> listPayroll(Integer month, Integer year) {
        return payslipRepository.findByMonthAndYear(month, year).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> listCurrentEmployeePayslips() {
        Employee employee = currentUserService.currentEmployee();
        return payslipRepository.findByEmployeeIdOrderByYearDescMonthDesc(employee.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> listCurrentEmployeePendingPayslips() {
        Employee employee = currentUserService.currentEmployee();
        return payslipRepository.findByEmployeeIdAndStatusOrderByYearDescMonthDesc(employee.getId(), PayslipStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PayslipResponse getCurrentEmployeePayslip(Long payslipId) {
        Employee employee = currentUserService.currentEmployee();
        return toResponse(findOwnedPayslip(payslipId, employee.getId()));
    }

    @Transactional(readOnly = true)
    public String downloadCurrentEmployeePayslip(Long payslipId) {
        Employee employee = currentUserService.currentEmployee();
        Payslip payslip = findOwnedPayslip(payslipId, employee.getId());
        return formatPayslipDetails(payslip);
    }

    @Transactional(readOnly = true)
    public EmailResponse emailCurrentEmployeePayslip(Long payslipId) {
        Employee employee = currentUserService.currentEmployee();
        Payslip payslip = findOwnedPayslip(payslipId, employee.getId());
        String subject = "Payslip %02d/%d - %s".formatted(
                payslip.getMonth(),
                payslip.getYear(),
                payslip.getEmployee().getEmployment().getInstitution()
        );
        String body = """
                Dear %s,

                Your payslip for %02d/%d is ready. Salary deductions have been applied as follows:

                %s
                """.formatted(
                employee.getFirstName(),
                payslip.getMonth(),
                payslip.getYear(),
                formatPayslipDetails(payslip)
        );
        return emailNotificationService.sendPayslipEmail(employee.getEmail(), subject, body);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> listCurrentEmployeeMessages() {
        Employee employee = currentUserService.currentEmployee();
        return messageRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId())
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    private Payslip computePayslip(Employment employment, PayrollRates rates, Integer month, Integer year) {
        BigDecimal base = scale(employment.getBaseSalary());
        BigDecimal house = percentage(base, rates.house());
        BigDecimal transport = percentage(base, rates.transport());
        BigDecimal gross = scale(base.add(house).add(transport));

        BigDecimal employeeTax = percentage(base, rates.employeeTax());
        BigDecimal pension = percentage(base, rates.pension());
        BigDecimal medicalInsurance = percentage(base, rates.medicalInsurance());
        BigDecimal others = percentage(base, rates.others());
        BigDecimal totalDeductions = employeeTax.add(pension).add(medicalInsurance).add(others);

        if (totalDeductions.compareTo(gross) > 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Total deductions exceed gross salary for employee " + employment.getEmployee().getEmployeeCode()
            );
        }

        Payslip payslip = new Payslip();
        payslip.setEmployee(employment.getEmployee());
        payslip.setMonth(month);
        payslip.setYear(year);
        payslip.setBaseSalary(base);
        payslip.setHouseAmount(house);
        payslip.setTransportAmount(transport);
        payslip.setGrossSalary(gross);
        payslip.setEmployeeTaxAmount(employeeTax);
        payslip.setPensionAmount(pension);
        payslip.setMedicalInsuranceAmount(medicalInsurance);
        payslip.setOtherAmount(others);
        payslip.setNetSalary(scale(gross.subtract(totalDeductions)));
        payslip.setStatus(PayslipStatus.PENDING);
        return payslip;
    }

    private PayrollRates loadRates() {
        return new PayrollRates(
                deductionService.requiredActiveRate(DeductionNames.EMPLOYEE_TAX),
                deductionService.requiredActiveRate(DeductionNames.PENSION),
                deductionService.requiredActiveRate(DeductionNames.MEDICAL_INSURANCE),
                deductionService.requiredActiveRate(DeductionNames.OTHERS),
                deductionService.requiredActiveRate(DeductionNames.HOUSE),
                deductionService.requiredActiveRate(DeductionNames.TRANSPORT)
        );
    }

    private BigDecimal percentage(BigDecimal amount, BigDecimal rate) {
        return scale(amount.multiply(rate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP));
    }

    private BigDecimal scale(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private Payslip findOwnedPayslip(Long payslipId, Long employeeId) {
        return payslipRepository.findByIdAndEmployeeId(payslipId, employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payslip was not found."));
    }

    private boolean isPostgreSql() {
        Boolean postgres = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql"));
        return Boolean.TRUE.equals(postgres);
    }

    private void markPaidAndCreateMessage(Payslip payslip) {
        payslip.setStatus(PayslipStatus.PAID);
        payslip.setPaidAt(LocalDateTime.now());
        messageRepository.save(buildMessage(payslip));
    }

    private Message buildMessage(Payslip payslip) {
        Message message = new Message();
        message.setEmployee(payslip.getEmployee());
        message.setPayslip(payslip);
        message.setMonth(payslip.getMonth());
        message.setYear(payslip.getYear());
        message.setContent(formatPaymentMessage(payslip));
        return message;
    }

    private void sendPaymentApprovalEmails(List<Payslip> payslips) {
        payslips.forEach(payslip -> {
            String subject = "Salary payment approved for %02d/%d".formatted(payslip.getMonth(), payslip.getYear());
            String body = """
                    Dear %s,

                    Your salary payment for %02d/%d from %s has been approved and credited successfully.

                    %s
                    """.formatted(
                    payslip.getEmployee().getFirstName(),
                    payslip.getMonth(),
                    payslip.getYear(),
                    payslip.getEmployee().getEmployment().getInstitution(),
                    formatPayslipDetails(payslip)
            );

            try {
                emailNotificationService.sendPaymentApprovedEmail(payslip.getEmployee().getEmail(), subject, body);
            } catch (ApiException exception) {
                log.warn(
                        "Could not send payment approval email to {} for payslip {}: {}",
                        payslip.getEmployee().getEmail(),
                        payslip.getId(),
                        exception.getMessage()
                );
            }
        });
    }

    private String formatPayslipDetails(Payslip payslip) {
        return """
                PAYSLIP %02d/%d
                Employee ID: %s
                Name: %s
                Institution: %s

                Base Salary: %s
                House Allowance: %s
                Transport Allowance: %s
                Gross Salary: %s

                Employee Tax Deducted: %s
                Pension Deducted: %s
                Medical Insurance Deducted: %s
                Other Deductions: %s

                Net Salary After Deductions: %s
                Status: %s
                """.formatted(
                payslip.getMonth(),
                payslip.getYear(),
                payslip.getEmployee().getEmployeeCode(),
                payslip.getEmployee().getFullName(),
                payslip.getEmployee().getEmployment().getInstitution(),
                payslip.getBaseSalary().toPlainString(),
                payslip.getHouseAmount().toPlainString(),
                payslip.getTransportAmount().toPlainString(),
                payslip.getGrossSalary().toPlainString(),
                payslip.getEmployeeTaxAmount().toPlainString(),
                payslip.getPensionAmount().toPlainString(),
                payslip.getMedicalInsuranceAmount().toPlainString(),
                payslip.getOtherAmount().toPlainString(),
                payslip.getNetSalary().toPlainString(),
                payslip.getStatus()
        );
    }

    private String formatPaymentMessage(Payslip payslip) {
        return "Dear %s Your salary of %02d/%d from %s %s has been credited to your %s account Successfully."
                .formatted(
                        payslip.getEmployee().getFirstName(),
                        payslip.getMonth(),
                        payslip.getYear(),
                        payslip.getEmployee().getEmployment().getInstitution(),
                        payslip.getNetSalary().toPlainString(),
                        payslip.getEmployee().getEmployeeCode()
                );
    }

    private PayslipResponse toResponse(Payslip payslip) {
        Employee employee = payslip.getEmployee();
        return new PayslipResponse(
                payslip.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmployment().getInstitution(),
                payslip.getBaseSalary(),
                payslip.getHouseAmount(),
                payslip.getTransportAmount(),
                payslip.getGrossSalary(),
                payslip.getEmployeeTaxAmount(),
                payslip.getPensionAmount(),
                payslip.getMedicalInsuranceAmount(),
                payslip.getOtherAmount(),
                payslip.getNetSalary(),
                payslip.getStatus(),
                payslip.getMonth(),
                payslip.getYear(),
                payslip.getPaidAt()
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getEmployee().getId(),
                message.getEmployee().getEmployeeCode(),
                message.getMonth(),
                message.getYear(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private record PayrollRates(
            BigDecimal employeeTax,
            BigDecimal pension,
            BigDecimal medicalInsurance,
            BigDecimal others,
            BigDecimal house,
            BigDecimal transport
    ) {
    }
}
