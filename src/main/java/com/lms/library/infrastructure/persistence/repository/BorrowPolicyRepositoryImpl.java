package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.domain.repository.BorrowPolicyRepository;
import com.lms.library.infrastructure.persistence.jpa.BorrowPolicyJpaEntity;
import com.lms.library.infrastructure.persistence.mapper.BorrowPolicyPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BorrowPolicyRepositoryImpl implements BorrowPolicyRepository {
    
    private final BorrowPolicyJpaRepository borrowPolicyJpaRepository;
    private final BorrowPolicyPersistenceMapper mapper;
    
    @Override
    public Optional<BorrowPolicy> findByMemberType(BorrowPolicy.MemberType memberType) {
        return borrowPolicyJpaRepository.findByMemberType(memberType)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<BorrowPolicy> findById(UUID id) {
        return borrowPolicyJpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public BorrowPolicy save(BorrowPolicy borrowPolicy) {
        BorrowPolicyJpaEntity entity = mapper.toJpaEntity(borrowPolicy);
        BorrowPolicyJpaEntity saved = borrowPolicyJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void deleteById(UUID id) {
        borrowPolicyJpaRepository.deleteById(id);
    }
}
