package com.lms.library.infrastructure.persistence.mapper;

import com.lms.library.domain.entity.User;
import com.lms.library.infrastructure.persistence.jpa.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    
    UserPersistenceMapper INSTANCE = Mappers.getMapper(UserPersistenceMapper.class);
    
    User toDomain(UserJpaEntity jpaEntity);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserJpaEntity toJpaEntity(User domain);
    
    List<User> toDomainList(List<UserJpaEntity> jpaEntities);
    
    List<UserJpaEntity> toJpaEntityList(List<User> domains);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateJpaEntityFromDomain(User domain, @MappingTarget UserJpaEntity jpaEntity);
}
