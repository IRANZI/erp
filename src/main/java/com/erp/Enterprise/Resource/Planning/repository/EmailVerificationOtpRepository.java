package com.erp.Enterprise.Resource.Planning.repository;

import com.erp.Enterprise.Resource.Planning.entity.EmailVerificationOtp;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {

    @EntityGraph(attributePaths = {"userAccount", "userAccount.employee"})
    @Query("""
            select otp
            from EmailVerificationOtp otp
            where lower(otp.userAccount.employee.email) = lower(:email)
              and otp.verifiedAt is null
            order by otp.createdAt desc
            """)
    List<EmailVerificationOtp> findLatestPendingByEmail(@Param("email") String email, Pageable pageable);
}
