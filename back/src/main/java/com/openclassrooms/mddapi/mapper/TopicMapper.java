package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.entity.TopicEntity;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

    public TopicDto toDto(TopicEntity entity) {
        if (entity == null) {
            return null;
        }
        return new TopicDto(entity.getId(), entity.getName(), entity.getDescription());
    }

    public TopicEntity toEntity(TopicDto dto) {
        if (dto == null) {
            return null;
        }
        TopicEntity entity = new TopicEntity();
        entity.setId(dto.id());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        return entity;
    }

    public TopicEntity reference(Long id) {
        if (id == null) {
            return null;
        }
        TopicEntity entity = new TopicEntity();
        entity.setId(id);
        return entity;
    }
}
