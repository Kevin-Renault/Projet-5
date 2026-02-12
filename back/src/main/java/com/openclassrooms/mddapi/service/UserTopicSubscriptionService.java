package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionId;
import com.openclassrooms.mddapi.mapper.UserTopicSubscriptionMapper;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserTopicSubscriptionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserTopicSubscriptionService {

    private final UserTopicSubscriptionRepository subscriptionRepository;
    private final MddUserRepository userRepository;
    private final TopicRepository topicRepository;
    private final UserTopicSubscriptionMapper mapper;

    public UserTopicSubscriptionService(
            UserTopicSubscriptionRepository subscriptionRepository,
            MddUserRepository userRepository,
            TopicRepository topicRepository,
            UserTopicSubscriptionMapper mapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UserTopicSubscriptionDto> getByUser(MddUserEntity principal, Long userId) {
        Long principalId = requireAuthenticatedUserId(principal);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!principalId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return subscriptionRepository.findAllByUser_Id(userId).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserTopicSubscriptionDto> getByTopic(Long topicId) {
        if (topicId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "topicId is required");
        }
        return subscriptionRepository.findAllByTopic_Id(topicId).stream().map(mapper::toDto).toList();
    }

    @Transactional
    public List<UserTopicSubscriptionDto> subscribe(MddUserEntity principal, Long userId, Long topicId) {
        Long principalId = requireAuthenticatedUserId(principal);
        if (userId == null || topicId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId and topicId are required");
        }
        if (!principalId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        if (!subscriptionRepository.existsByUser_IdAndTopic_Id(userId, topicId)) {
            UserTopicSubscriptionId id = new UserTopicSubscriptionId();
            id.setUserId(userId);
            id.setTopicId(topicId);

            UserTopicSubscriptionEntity entity = new UserTopicSubscriptionEntity();
            entity.setId(id);
            entity.setUser(userRepository.getReferenceById(userId));
            entity.setTopic(topic);

            subscriptionRepository.save(entity);
        }

        return subscriptionRepository.findAllByUser_Id(userId).stream().map(mapper::toDto).toList();
    }

    @Transactional
    public List<UserTopicSubscriptionDto> unsubscribe(MddUserEntity principal, Long userId, Long topicId) {
        Long principalId = requireAuthenticatedUserId(principal);
        if (userId == null || topicId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId and topicId are required");
        }
        if (!principalId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        subscriptionRepository.deleteByUser_IdAndTopic_Id(userId, topicId);
        return subscriptionRepository.findAllByUser_Id(userId).stream().map(mapper::toDto).toList();
    }

    private static Long requireAuthenticatedUserId(MddUserEntity principal) {
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal.getId();
    }
}
