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
        Long topicId = entity.getTopic() != null ? entity.getTopic().getId() : null;
        if (topicId == null) {
            return null;
        }
        return new UserTopicSubscriptionDto(topicId);
    }

    public UserTopicSubscriptionEntity toEntity(UserTopicSubscriptionDto dto, Long userId) {
        if (dto == null || userId == null) {
            return null;
        }
        UserTopicSubscriptionEntity entity = new UserTopicSubscriptionEntity();
        UserTopicSubscriptionId id = new UserTopicSubscriptionId();
        id.setUserId(userId);
        id.setTopicId(dto.topicId());
        entity.setId(id);

        MddUserEntity user = new MddUserEntity();
        user.setId(userId);
        entity.setUser(user);

        TopicEntity topic = new TopicEntity();
        topic.setId(dto.topicId());
        entity.setTopic(topic);

        entity.setSubscribedAt(java.time.Instant.now());

        return entity;
    }
}
