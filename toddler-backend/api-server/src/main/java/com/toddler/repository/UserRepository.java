package com.toddler.repository;

import com.toddler.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findById(UUID uuid);
    Set<UserEntity> findAllByIdIn(Set<UUID> ids);
}
