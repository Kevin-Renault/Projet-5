package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionId;
import org.springframework.stereotype.Component;

@Component
public class UserTopicSubscriptionMapper {

    public UserTopicSubscriptionDto toDto(UserTopicSubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        Long userId = entity.getUser() != null ? entity.getUser().getId() : null;
        Long topicId = entity.getTopic() != null ? entity.getTopic().getId() : null;
        if (entity.getId() != null) {
            if (userId == null) {
                userId = entity.getId().getUserId();
            }
            if (topicId == null) {
                topicId = entity.getId().getTopicId();
            }
        }
        return new UserTopicSubscriptionDto(userId, topicId, entity.getSubscribedAt());
    }

    public UserTopicSubscriptionEntity toEntity(UserTopicSubscriptionDto dto) {
        if (dto == null) {
            return null;
        }
        UserTopicSubscriptionEntity entity = new UserTopicSubscriptionEntity();

        if (dto.userId() != null && dto.topicId() != null) {
            UserTopicSubscriptionId id = new UserTopicSubscriptionId();
            id.setUserId(dto.userId());
            id.setTopicId(dto.topicId());
            entity.setId(id);
        }

        if (dto.userId() != null) {
            MddUserEntity user = new MddUserEntity();
            user.setId(dto.userId());
            entity.setUser(user);
        }

        if (dto.topicId() != null) {
            TopicEntity topic = new TopicEntity();
            topic.setId(dto.topicId());
            entity.setTopic(topic);
        }

        entity.setSubscribedAt(dto.subscribedAt());
        return entity;
    }
}
