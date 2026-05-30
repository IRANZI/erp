package com.erp.Enterprise.Resource.Planning.security;

import com.erp.Enterprise.Resource.Planning.entity.UserAccount;
import com.erp.Enterprise.Resource.Planning.entity.UserStatus;
import com.erp.Enterprise.Resource.Planning.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountDetailsService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByEmployeeEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(account.getEmployee().getEmail())
                .password(account.getPassword())
                .disabled(account.getStatus() != UserStatus.ACTIVE)
                .authorities(account.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .build();
    }
}
