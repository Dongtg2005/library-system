package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.Fine;
import com.lms.library.domain.repository.FineRepository;
import com.lms.library.infrastructure.persistence.jpa.FineJpaEntity;
import com.lms.library.infrastructure.persistence.mapper.FinePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FineRepositoryImpl implements FineRepository {
    
    private final FineJpaRepository fineJpaRepository;
    private final FinePersistenceMapper mapper;
    
    @Override
    public Fine save(Fine fine) {
        FineJpaEntity entity = mapper.toJpaEntity(fine);
        FineJpaEntity saved = fineJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Fine> findById(UUID id) {
        return fineJpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Fine> findByBorrowRecordId(UUID borrowRecordId) {
        return fineJpaRepository.findByBorrowRecordId(borrowRecordId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public List<Fine> findByMemberId(UUID memberId) {
        return fineJpaRepository.findByMemberId(memberId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public List<Fine> findByStatus(Fine.FineStatus status) {
        return fineJpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(UUID id) {
        fineJpaRepository.deleteById(id);
    }
}
