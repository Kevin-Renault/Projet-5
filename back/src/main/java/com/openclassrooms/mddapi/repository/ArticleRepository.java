package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.ArticleEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {
    List<ArticleEntity> findAllByTopic_IdOrderByCreatedAtDesc(Long topicId);

    List<ArticleEntity> findAllByAuthor_IdOrderByCreatedAtDesc(Long authorId);
}
