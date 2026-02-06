package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.entity.ArticleEntity;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {

    public ArticleDto toDto(ArticleEntity entity) {
        if (entity == null) {
            return null;
        }
        Long authorId = entity.getAuthor() != null ? entity.getAuthor().getId() : null;
        Long topicId = entity.getTopic() != null ? entity.getTopic().getId() : null;
        return new ArticleDto(entity.getId(), entity.getTitle(), entity.getContent(), entity.getCreatedAt(), authorId,
                topicId);
    }

    public ArticleEntity toEntity(ArticleDto dto) {
        if (dto == null) {
            return null;
        }
        ArticleEntity entity = new ArticleEntity();
        entity.setId(dto.id());
        entity.setTitle(dto.title());
        entity.setContent(dto.content());
        entity.setCreatedAt(dto.createdAt());

        if (dto.authorId() != null) {
            MddUserEntity author = new MddUserEntity();
            author.setId(dto.authorId());
            entity.setAuthor(author);
        }
        if (dto.topicId() != null) {
            TopicEntity topic = new TopicEntity();
            topic.setId(dto.topicId());
            entity.setTopic(topic);
        }
        return entity;
    }
}
