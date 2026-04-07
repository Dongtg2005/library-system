package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.infrastructure.persistence.jpa.BorrowRecordJpaEntity;
import com.lms.library.infrastructure.persistence.mapper.BorrowRecordPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BorrowRecordRepositoryImpl implements BorrowRecordRepository {
    
    private final BorrowRecordJpaRepository borrowRecordJpaRepository;
    private final BorrowRecordPersistenceMapper mapper;
    
    @Override
    public BorrowRecord save(BorrowRecord borrowRecord) {
        BorrowRecordJpaEntity entity = mapper.toJpaEntity(borrowRecord);
        BorrowRecordJpaEntity saved = borrowRecordJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<BorrowRecord> findById(UUID id) {
        return borrowRecordJpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<BorrowRecord> findByMemberId(UUID memberId) {
        return borrowRecordJpaRepository.findByMemberId(memberId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public int countByMemberIdAndBorrowStatusIn(UUID memberId, Collection<BorrowRecord.BorrowStatus> borrowStatus) {
        return borrowRecordJpaRepository.countByMemberIdAndBorrowStatusIn(memberId, borrowStatus);
    }
    
    @Override
    public List<BorrowRecord> findByBookId(UUID bookId) {
        return borrowRecordJpaRepository.findByBookId(bookId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public List<BorrowRecord> findOverdueRecords() {
        return borrowRecordJpaRepository.findOverdueRecords()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(UUID id) {
        borrowRecordJpaRepository.deleteById(id);
    }
}
