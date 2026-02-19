package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionEntity;
import com.openclassrooms.mddapi.mapper.UserTopicSubscriptionMapper;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserTopicSubscriptionRepository;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserTopicSubscriptionServiceTest {

    @Mock
    private UserTopicSubscriptionRepository subscriptionRepository;

    @Mock
    private MddUserRepository userRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserTopicSubscriptionMapper mapper;

    @InjectMocks
    private UserTopicSubscriptionService service;

    @Test
    void getByUser_requires_authentication() {
        Assertions.assertThatThrownBy(() -> service.getByUser(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    @Test
    void subscribe_requires_topicId_and_existing_topic() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Assertions.assertThatThrownBy(() -> service.subscribe(principal, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        Mockito.when(topicRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> service.subscribe(principal, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void subscribe_is_idempotent_and_returns_list() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        TopicEntity topic = new TopicEntity();
        topic.setId(2L);
        Mockito.when(topicRepository.findById(2L)).thenReturn(Optional.of(topic));

        Mockito.when(subscriptionRepository.existsByUser_IdAndTopic_Id(1L, 2L)).thenReturn(true);
        Mockito.when(subscriptionRepository.findAllByUser_Id(1L)).thenReturn(List.of());

        List<UserTopicSubscriptionDto> out = service.subscribe(principal, 2L);
        Assertions.assertThat(out).isEmpty();
        Mockito.verify(subscriptionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void subscribe_creates_when_missing() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        TopicEntity topic = new TopicEntity();
        topic.setId(2L);
        Mockito.when(topicRepository.findById(2L)).thenReturn(Optional.of(topic));

        Mockito.when(subscriptionRepository.existsByUser_IdAndTopic_Id(1L, 2L)).thenReturn(false);

        MddUserEntity userRef = new MddUserEntity();
        userRef.setId(1L);
        Mockito.when(userRepository.getReferenceById(1L)).thenReturn(userRef);

        UserTopicSubscriptionEntity returnedEntity = new UserTopicSubscriptionEntity();
        returnedEntity.setUser(userRef);
        returnedEntity.setTopic(topic);
        Mockito.when(subscriptionRepository.findAllByUser_Id(1L)).thenReturn(List.of(returnedEntity));
        Mockito.when(mapper.toDto(returnedEntity)).thenReturn(new UserTopicSubscriptionDto(2L));

        List<UserTopicSubscriptionDto> out = service.subscribe(principal, 2L);
        Assertions.assertThat(out).containsExactly(new UserTopicSubscriptionDto(2L));

        ArgumentCaptor<UserTopicSubscriptionEntity> entityCaptor = ArgumentCaptor
                .forClass(UserTopicSubscriptionEntity.class);
        Mockito.verify(subscriptionRepository).save(entityCaptor.capture());
        Assertions.assertThat(entityCaptor.getValue().getUser()).isSameAs(userRef);
        Assertions.assertThat(entityCaptor.getValue().getTopic()).isSameAs(topic);
        Assertions.assertThat(entityCaptor.getValue().getId()).isNotNull();
        Assertions.assertThat(entityCaptor.getValue().getId().getUserId()).isEqualTo(1L);
        Assertions.assertThat(entityCaptor.getValue().getId().getTopicId()).isEqualTo(2L);
    }

    @Test
    void unsubscribe_deletes_and_returns_list() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        UserTopicSubscriptionEntity entity = new UserTopicSubscriptionEntity();
        Mockito.when(subscriptionRepository.findAllByUser_Id(1L)).thenReturn(List.of(entity));
        Mockito.when(mapper.toDto(entity)).thenReturn(new UserTopicSubscriptionDto(2L));

        List<UserTopicSubscriptionDto> out = service.unsubscribe(principal, 2L);
        Assertions.assertThat(out).containsExactly(new UserTopicSubscriptionDto(2L));

        Mockito.verify(subscriptionRepository).deleteByUser_IdAndTopic_Id(1L, 2L);
    }
}
