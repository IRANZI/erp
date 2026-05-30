package com.erp.Enterprise.Resource.Planning.config;

import com.erp.Enterprise.Resource.Planning.entity.Deduction;
import com.erp.Enterprise.Resource.Planning.entity.Employee;
import com.erp.Enterprise.Resource.Planning.entity.Employment;
import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import com.erp.Enterprise.Resource.Planning.entity.Role;
import com.erp.Enterprise.Resource.Planning.entity.UserAccount;
import com.erp.Enterprise.Resource.Planning.entity.UserStatus;
import com.erp.Enterprise.Resource.Planning.repository.DeductionRepository;
import com.erp.Enterprise.Resource.Planning.repository.EmployeeRepository;
import com.erp.Enterprise.Resource.Planning.service.DeductionNames;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final DeductionRepository deductionRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemoData;

    @Override
    @Transactional
    public void run(String... args) {
        seedDeduction(DeductionNames.EMPLOYEE_TAX, "30");
        seedDeduction(DeductionNames.PENSION, "6");
        seedDeduction(DeductionNames.MEDICAL_INSURANCE, "5");
        seedDeduction(DeductionNames.OTHERS, "5");
        seedDeduction(DeductionNames.HOUSE, "14");
        seedDeduction(DeductionNames.TRANSPORT, "14");

        if (seedDemoData) {
            seedEmployee(
                    "ADMIN001",
                    "System",
                    "Admin",
                    "admin@rca.gov.rw",
                    "Admin@123",
                    new BigDecimal("900000"),
                    Set.of(Role.ROLE_ADMIN)
            );
            seedEmployee(
                    "MGR001",
                    "Payroll",
                    "Manager",
                    "manager@rca.gov.rw",
                    "Manager@123",
                    new BigDecimal("750000"),
                    Set.of(Role.ROLE_MANAGER)
            );
            seedEmployee(
                    "EMP001",
                    "Peter",
                    "Mugabo",
                    "peter@rca.gov.rw",
                    "Employee@123",
                    new BigDecimal("70000"),
                    Set.of(Role.ROLE_EMPLOYEE)
            );
        }
    }

    private void seedDeduction(String name, String rate) {
        deductionRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Deduction deduction = new Deduction();
            deduction.setName(name);
            deduction.setRatePercent(new BigDecimal(rate));
            deduction.setActive(true);
            return deductionRepository.save(deduction);
        });
    }

    private void seedEmployee(
            String employeeCode,
            String firstName,
            String lastName,
            String email,
            String password,
            BigDecimal baseSalary,
            Set<Role> roles
    ) {
        if (employeeRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Employee employee = new Employee();
        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setDistrict("Nyarugenge");
        employee.setMobile("+250780000000");
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));

        Employment employment = new Employment();
        employment.setInstitution("RCA");
        employment.setDepartment("ICT");
        employment.setPosition(lastName);
        employment.setBaseSalary(baseSalary);
        employment.setStatus(EmploymentStatus.ACTIVE);
        employment.setJoiningDate(LocalDate.of(2024, 1, 1));

        UserAccount account = new UserAccount();
        account.setPassword(passwordEncoder.encode(password));
        account.setStatus(UserStatus.ACTIVE);
        account.setRoles(roles);

        employee.assignEmployment(employment);
        employee.assignUserAccount(account);
        employeeRepository.save(employee);
    }
}
