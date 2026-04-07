package com.lms.library.infrastructure.persistence.repository;

import com.lms.library.domain.entity.UserProfile;
import com.lms.library.domain.repository.UserProfileRepository;
import com.lms.library.infrastructure.persistence.jpa.UserProfileJpaEntity;
import com.lms.library.infrastructure.persistence.mapper.UserProfilePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {
    
    private final UserProfileJpaRepository userProfileJpaRepository;
    private final UserProfilePersistenceMapper mapper;
    
    @Override
    public Optional<UserProfile> findByAuthUserId(Long authUserId) {
        return userProfileJpaRepository.findByAuthUserId(authUserId)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<UserProfile> findByEmail(String email) {
        return userProfileJpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }
    
    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileJpaEntity entity = mapper.toJpaEntity(userProfile);
        UserProfileJpaEntity saved = userProfileJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<UserProfile> findById(Long id) {
        return userProfileJpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<UserProfile> findAll() {
        return userProfileJpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(Long id) {
        userProfileJpaRepository.deleteById(id);
    }
}
