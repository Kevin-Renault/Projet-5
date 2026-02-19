package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.entity.ArticleCommentEntity;
import com.openclassrooms.mddapi.entity.ArticleEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionEntity;
import com.openclassrooms.mddapi.entity.UserTopicSubscriptionId;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MappersTest {

    private final TopicMapper topicMapper = new TopicMapper();
    private final ArticleMapper articleMapper = new ArticleMapper();
    private final CommentMapper commentMapper = new CommentMapper();
    private final UserMapper userMapper = new UserMapper();
    private final UserTopicSubscriptionMapper subscriptionMapper = new UserTopicSubscriptionMapper();

    @Test
    void topicMapper_maps_both_ways_and_reference() {
        Assertions.assertThat(topicMapper.toDto(null)).isNull();
        Assertions.assertThat(topicMapper.toEntity(null)).isNull();
        Assertions.assertThat(topicMapper.reference(null)).isNull();

        TopicEntity entity = new TopicEntity();
        entity.setId(1L);
        entity.setName("Java");
        entity.setDescription("Desc");

        TopicDto dto = topicMapper.toDto(entity);
        Assertions.assertThat(dto).isEqualTo(new TopicDto(1L, "Java", "Desc"));

        TopicEntity back = topicMapper.toEntity(dto);
        Assertions.assertThat(back.getId()).isEqualTo(1L);
        Assertions.assertThat(back.getName()).isEqualTo("Java");
        Assertions.assertThat(back.getDescription()).isEqualTo("Desc");

        TopicEntity ref = topicMapper.reference(99L);
        Assertions.assertThat(ref.getId()).isEqualTo(99L);
        Assertions.assertThat(ref.getName()).isNull();
    }

    @Test
    void articleMapper_handles_nulls_and_nested_ids() {
        Assertions.assertThat(articleMapper.toDto(null)).isNull();
        Assertions.assertThat(articleMapper.toEntity(null)).isNull();

        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        ArticleEntity entity = new ArticleEntity();
        entity.setId(10L);
        entity.setTitle("T");
        entity.setContent("C");
        entity.setCreatedAt(now);

        ArticleDto dtoNoNested = articleMapper.toDto(entity);
        Assertions.assertThat(dtoNoNested.authorId()).isNull();
        Assertions.assertThat(dtoNoNested.topicId()).isNull();

        MddUserEntity author = new MddUserEntity();
        author.setId(2L);
        TopicEntity topic = new TopicEntity();
        topic.setId(3L);
        entity.setAuthor(author);
        entity.setTopic(topic);

        ArticleDto dto = articleMapper.toDto(entity);
        Assertions.assertThat(dto.authorId()).isEqualTo(2L);
        Assertions.assertThat(dto.topicId()).isEqualTo(3L);

        ArticleEntity back = articleMapper.toEntity(dto);
        Assertions.assertThat(back.getAuthor()).isNotNull();
        Assertions.assertThat(back.getAuthor().getId()).isEqualTo(2L);
        Assertions.assertThat(back.getTopic()).isNotNull();
        Assertions.assertThat(back.getTopic().getId()).isEqualTo(3L);
    }

    @Test
    void commentMapper_handles_nulls_and_nested_ids() {
        Assertions.assertThat(commentMapper.toDto(null)).isNull();
        Assertions.assertThat(commentMapper.toEntity(null)).isNull();

        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        ArticleCommentEntity entity = new ArticleCommentEntity();
        entity.setId(5L);
        entity.setContent("Hi");
        entity.setCreatedAt(now);

        CommentDto dtoNoNested = commentMapper.toDto(entity);
        Assertions.assertThat(dtoNoNested.authorId()).isNull();
        Assertions.assertThat(dtoNoNested.articleId()).isNull();

        MddUserEntity author = new MddUserEntity();
        author.setId(7L);
        ArticleEntity article = new ArticleEntity();
        article.setId(8L);
        entity.setAuthor(author);
        entity.setArticle(article);

        CommentDto dto = commentMapper.toDto(entity);
        Assertions.assertThat(dto.authorId()).isEqualTo(7L);
        Assertions.assertThat(dto.articleId()).isEqualTo(8L);

        ArticleCommentEntity back = commentMapper.toEntity(dto);
        Assertions.assertThat(back.getAuthor()).isNotNull();
        Assertions.assertThat(back.getAuthor().getId()).isEqualTo(7L);
        Assertions.assertThat(back.getArticle()).isNotNull();
        Assertions.assertThat(back.getArticle().getId()).isEqualTo(8L);
    }

    @Test
    void userMapper_maps_both_ways_and_reference() {
        Assertions.assertThat(userMapper.toDto(null)).isNull();
        Assertions.assertThat(userMapper.toEntity(null)).isNull();
        Assertions.assertThat(userMapper.reference(null)).isNull();

        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        MddUserEntity entity = new MddUserEntity();
        entity.setId(1L);
        entity.setUsername("u");
        entity.setEmail("e@example.com");
        entity.setCreatedAt(now);

        UserDto dto = userMapper.toDto(entity);
        Assertions.assertThat(dto.id()).isEqualTo(1L);
        Assertions.assertThat(dto.username()).isEqualTo("u");
        Assertions.assertThat(dto.email()).isEqualTo("e@example.com");
        Assertions.assertThat(dto.role()).isEqualTo("user");
        Assertions.assertThat(dto.createdAt()).isEqualTo(now);

        MddUserEntity back = userMapper.toEntity(dto);
        Assertions.assertThat(back.getId()).isEqualTo(1L);
        Assertions.assertThat(back.getUsername()).isEqualTo("u");
        Assertions.assertThat(back.getEmail()).isEqualTo("e@example.com");
        Assertions.assertThat(back.getCreatedAt()).isEqualTo(now);

        MddUserEntity ref = userMapper.reference(9L);
        Assertions.assertThat(ref.getId()).isEqualTo(9L);
    }

    @Test
    void subscriptionMapper_toDto_returns_null_when_topic_missing() {
        Assertions.assertThat(subscriptionMapper.toDto(null)).isNull();

        UserTopicSubscriptionEntity e = new UserTopicSubscriptionEntity();
        Assertions.assertThat(subscriptionMapper.toDto(e)).isNull();

        TopicEntity t = new TopicEntity();
        t.setId(null);
        e.setTopic(t);
        Assertions.assertThat(subscriptionMapper.toDto(e)).isNull();

        t.setId(3L);
        Assertions.assertThat(subscriptionMapper.toDto(e)).isEqualTo(new UserTopicSubscriptionDto(3L));
    }

    @Test
    void subscriptionMapper_toEntity_sets_composite_id_and_relations() {
        UserTopicSubscriptionEntity entity = subscriptionMapper.toEntity(new UserTopicSubscriptionDto(5L), 7L);
        Assertions.assertThat(entity).isNotNull();
        Assertions.assertThat(entity.getId()).isNotNull();
        Assertions.assertThat(entity.getId().getUserId()).isEqualTo(7L);
        Assertions.assertThat(entity.getId().getTopicId()).isEqualTo(5L);
        Assertions.assertThat(entity.getUser()).isNotNull();
        Assertions.assertThat(entity.getUser().getId()).isEqualTo(7L);
        Assertions.assertThat(entity.getTopic()).isNotNull();
        Assertions.assertThat(entity.getTopic().getId()).isEqualTo(5L);
        Assertions.assertThat(entity.getSubscribedAt()).isNotNull();

        Assertions.assertThat(subscriptionMapper.toEntity(null, 7L)).isNull();
        Assertions.assertThat(subscriptionMapper.toEntity(new UserTopicSubscriptionDto(5L), null)).isNull();
    }

    @Test
    void subscriptionId_equals_hashCode_is_consistent() {
        UserTopicSubscriptionId a = new UserTopicSubscriptionId();
        a.setUserId(1L);
        a.setTopicId(2L);

        UserTopicSubscriptionId b = new UserTopicSubscriptionId();
        b.setUserId(1L);
        b.setTopicId(2L);

        Assertions.assertThat(a).isEqualTo(b);
        Assertions.assertThat(a.hashCode()).isEqualTo(b.hashCode());

        b.setTopicId(3L);
        Assertions.assertThat(a).isNotEqualTo(b);
    }
}
