package com.lms.library.infrastructure.persistence.mapper;

import com.lms.library.domain.entity.BorrowPolicy;
import com.lms.library.infrastructure.persistence.jpa.BorrowPolicyJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BorrowPolicyPersistenceMapper {
    
    BorrowPolicyPersistenceMapper INSTANCE = Mappers.getMapper(BorrowPolicyPersistenceMapper.class);
    
    BorrowPolicy toDomain(BorrowPolicyJpaEntity jpaEntity);
    
    @Mapping(target = "id", ignore = true)
    BorrowPolicyJpaEntity toJpaEntity(BorrowPolicy domain);
    
    List<BorrowPolicy> toDomainList(List<BorrowPolicyJpaEntity> jpaEntities);
    
    List<BorrowPolicyJpaEntity> toJpaEntityList(List<BorrowPolicy> domains);
    
    @Mapping(target = "id", ignore = true)
    void updateJpaEntityFromDomain(BorrowPolicy domain, @MappingTarget BorrowPolicyJpaEntity jpaEntity);
}
