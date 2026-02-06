package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.ArticleCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleCommentRepository extends JpaRepository<ArticleCommentEntity, Long> {
    List<ArticleCommentEntity> findAllByArticle_IdOrderByCreatedAtAsc(Long articleId);
}
