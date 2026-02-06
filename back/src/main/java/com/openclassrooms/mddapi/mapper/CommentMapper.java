package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.entity.ArticleEntity;
import com.openclassrooms.mddapi.entity.ArticleCommentEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(ArticleCommentEntity entity) {
        if (entity == null) {
            return null;
        }
        Long authorId = entity.getAuthor() != null ? entity.getAuthor().getId() : null;
        Long articleId = entity.getArticle() != null ? entity.getArticle().getId() : null;
        return new CommentDto(entity.getId(), entity.getContent(), entity.getCreatedAt(), authorId, articleId);
    }

    public ArticleCommentEntity toEntity(CommentDto dto) {
        if (dto == null) {
            return null;
        }
        ArticleCommentEntity entity = new ArticleCommentEntity();
        entity.setId(dto.id());
        entity.setContent(dto.content());
        entity.setCreatedAt(dto.createdAt());

        if (dto.authorId() != null) {
            MddUserEntity author = new MddUserEntity();
            author.setId(dto.authorId());
            entity.setAuthor(author);
        }
        if (dto.articleId() != null) {
            ArticleEntity article = new ArticleEntity();
            article.setId(dto.articleId());
            entity.setArticle(article);
        }
        return entity;
    }
}
