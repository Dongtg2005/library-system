package com.lms.library.config;

import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.domain.repository.BorrowPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private final BorrowPolicyRepository borrowPolicyRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var existing = borrowPolicyRepository.findAllByMemberTypeOrderByCreatedAtDesc(BorrowPolicy.MemberType.USER);
        if (existing == null || existing.isEmpty()) {
            BorrowPolicy policy = BorrowPolicy.builder()
                    .name("Default USER policy")
                    .memberType(BorrowPolicy.MemberType.USER)
                    .maxBooksAllowed(5)
                    .loanPeriodDays(14)
                    .maxExtensions(2)
                    .finePerDay(new BigDecimal("1000.00"))
                    .gracePeriodDays(0)
                    .isActive(true)
                    .build();

            borrowPolicyRepository.save(policy);
        }
    }
}
