package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.UserTopicSubscriptionEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTopicSubscriptionRepository
        extends JpaRepository<UserTopicSubscriptionEntity, UserTopicSubscriptionId> {

    boolean existsByUser_IdAndTopic_Id(Long userId, Long topicId);

    void deleteByUser_IdAndTopic_Id(Long userId, Long topicId);

    List<UserTopicSubscriptionEntity> findAllByUser_Id(Long userId);

    List<UserTopicSubscriptionEntity> findAllByTopic_Id(Long topicId);
}
