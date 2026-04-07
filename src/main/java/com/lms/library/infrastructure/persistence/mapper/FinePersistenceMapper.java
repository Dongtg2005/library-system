package com.lms.library.infrastructure.persistence.mapper;

import com.lms.library.domain.entity.Fine;
import com.lms.library.infrastructure.persistence.jpa.FineJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinePersistenceMapper {
    
    FinePersistenceMapper INSTANCE = Mappers.getMapper(FinePersistenceMapper.class);
    
    Fine toDomain(FineJpaEntity jpaEntity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FineJpaEntity toJpaEntity(Fine domain);
    
    List<Fine> toDomainList(List<FineJpaEntity> jpaEntities);
    
    List<FineJpaEntity> toJpaEntityList(List<Fine> domains);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateJpaEntityFromDomain(Fine domain, @MappingTarget FineJpaEntity jpaEntity);
}
