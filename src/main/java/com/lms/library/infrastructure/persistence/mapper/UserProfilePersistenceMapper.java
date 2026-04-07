package com.lms.library.infrastructure.persistence.mapper;

import com.lms.library.domain.entity.UserProfile;
import com.lms.library.infrastructure.persistence.jpa.UserProfileJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserProfilePersistenceMapper {
    
    UserProfilePersistenceMapper INSTANCE = Mappers.getMapper(UserProfilePersistenceMapper.class);
    
    UserProfile toDomain(UserProfileJpaEntity jpaEntity);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserProfileJpaEntity toJpaEntity(UserProfile domain);
    
    List<UserProfile> toDomainList(List<UserProfileJpaEntity> jpaEntities);
    
    List<UserProfileJpaEntity> toJpaEntityList(List<UserProfile> domains);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateJpaEntityFromDomain(UserProfile domain, @MappingTarget UserProfileJpaEntity jpaEntity);
}
