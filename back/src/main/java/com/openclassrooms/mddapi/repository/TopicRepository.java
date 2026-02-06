package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.TopicEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<TopicEntity, Long> {
    Optional<TopicEntity> findByName(String name);
}
