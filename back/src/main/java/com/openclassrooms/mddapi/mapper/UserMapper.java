package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(MddUserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserDto(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getCreatedAt());
    }

    /**
     * Note: le DTO ne contient pas le mot de passe.
     * Cette conversion ne renseigne donc pas le champ password.
     */
    public MddUserEntity toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        MddUserEntity entity = new MddUserEntity();
        entity.setId(dto.id());
        entity.setUsername(dto.username());
        entity.setEmail(dto.email());
        entity.setCreatedAt(dto.createdAt());
        return entity;
    }

    public MddUserEntity reference(Long id) {
        if (id == null) {
            return null;
        }
        MddUserEntity entity = new MddUserEntity();
        entity.setId(id);
        return entity;
    }
}
