package com.lms.library.infrastructure.persistence.mapper;

import com.lms.library.domain.entity.Book;
import com.lms.library.infrastructure.persistence.jpa.BookJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookPersistenceMapper {
    
    BookPersistenceMapper INSTANCE = Mappers.getMapper(BookPersistenceMapper.class);
    
    Book toDomain(BookJpaEntity jpaEntity);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BookJpaEntity toJpaEntity(Book domain);
    
    List<Book> toDomainList(List<BookJpaEntity> jpaEntities);
    
    List<BookJpaEntity> toJpaEntityList(List<Book> domains);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateJpaEntityFromDomain(Book domain, @MappingTarget BookJpaEntity jpaEntity);
}
