package com.lms.library.infrastructure.persistence.mapper;

import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.infrastructure.persistence.jpa.BorrowRecordJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BorrowRecordPersistenceMapper {
    
    BorrowRecordPersistenceMapper INSTANCE = Mappers.getMapper(BorrowRecordPersistenceMapper.class);
    
    BorrowRecord toDomain(BorrowRecordJpaEntity jpaEntity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "borrowDate", ignore = true)
    @Mapping(target = "borrowTime", ignore = true)
    BorrowRecordJpaEntity toJpaEntity(BorrowRecord domain);
    
    List<BorrowRecord> toDomainList(List<BorrowRecordJpaEntity> jpaEntities);
    
    List<BorrowRecordJpaEntity> toJpaEntityList(List<BorrowRecord> domains);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "borrowDate", ignore = true)
    @Mapping(target = "borrowTime", ignore = true)
    void updateJpaEntityFromDomain(BorrowRecord domain, @MappingTarget BorrowRecordJpaEntity jpaEntity);
}
