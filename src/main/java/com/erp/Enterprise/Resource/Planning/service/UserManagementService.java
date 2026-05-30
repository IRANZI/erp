package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.dto.UserSummaryResponse;
import com.erp.Enterprise.Resource.Planning.entity.Role;
import com.erp.Enterprise.Resource.Planning.entity.UserAccount;
import com.erp.Enterprise.Resource.Planning.entity.UserStatus;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import com.erp.Enterprise.Resource.Planning.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> listUsers() {
        return userAccountRepository.findAll()
                .stream()
                .distinct()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public UserSummaryResponse updateStatus(Long id, UserStatus status) {
        UserAccount account = findUser(id);
        account.setStatus(status);
        return toSummary(account);
    }

    @Transactional
    public UserSummaryResponse updateRoles(Long id, Set<Role> roles) {
        UserAccount account = findUser(id);
        account.setRoles(new LinkedHashSet<>(roles));
        return toSummary(account);
    }

    private UserAccount findUser(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User was not found."));
    }

    private UserSummaryResponse toSummary(UserAccount account) {
        return new UserSummaryResponse(
                account.getId(),
                account.getEmployee().getId(),
                account.getEmployee().getEmployeeCode(),
                account.getEmployee().getFullName(),
                account.getEmployee().getEmail(),
                account.getStatus(),
                account.getRoles()
        );
    }
}
