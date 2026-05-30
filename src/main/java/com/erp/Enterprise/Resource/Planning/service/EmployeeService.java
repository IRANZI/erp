package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.dto.CreateEmployeeRequest;
import com.erp.Enterprise.Resource.Planning.dto.EmployeeResponse;
import com.erp.Enterprise.Resource.Planning.entity.Employee;
import com.erp.Enterprise.Resource.Planning.entity.Employment;
import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import com.erp.Enterprise.Resource.Planning.entity.Role;
import com.erp.Enterprise.Resource.Planning.entity.UserAccount;
import com.erp.Enterprise.Resource.Planning.entity.UserStatus;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import com.erp.Enterprise.Resource.Planning.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
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
        employment.setStatus(request.status() == null ? EmploymentStatus.ACTIVE : request.status());
        employment.setJoiningDate(request.joiningDate());

        UserAccount account = new UserAccount();
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setStatus(UserStatus.ACTIVE);
        account.setRoles(resolveRoles(request.roles()));

        employee.assignEmployment(employment);
        employee.assignUserAccount(account);

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> listEmployees() {
        return employeeRepository.findAll()
                .stream()
                .distinct()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        return toResponse(findEmployee(id));
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee was not found."));
    }

    @Transactional
    public EmployeeResponse updateEmploymentStatus(Long employeeId, EmploymentStatus status) {
        Employee employee = findEmployee(employeeId);
        employee.getEmployment().setStatus(status);
        return toResponse(employee);
    }

    public EmployeeResponse toResponse(Employee employee) {
        Employment employment = employee.getEmployment();
        UserAccount account = employee.getUserAccount();
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getDistrict(),
                employee.getMobile(),
                employee.getDateOfBirth(),
                employment.getInstitution(),
                employment.getDepartment(),
                employment.getPosition(),
                employment.getBaseSalary(),
                employment.getStatus(),
                employment.getJoiningDate(),
                account.getId(),
                account.getStatus(),
                account.getRoles()
        );
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findWithEmploymentById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee was not found."));
    }

    private Set<Role> resolveRoles(Set<Role> requestedRoles) {
        Set<Role> roles = new LinkedHashSet<>();
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            roles.add(Role.ROLE_EMPLOYEE);
        } else {
            roles.addAll(requestedRoles);
        }
        return roles;
    }
}
