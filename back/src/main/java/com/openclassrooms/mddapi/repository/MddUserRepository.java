package com.openclassrooms.mddapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.entity.MddUserEntity;

public interface MddUserRepository extends JpaRepository<MddUserEntity, Long> {
    Optional<MddUserEntity> findByEmail(String email);

    Optional<MddUserEntity> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
