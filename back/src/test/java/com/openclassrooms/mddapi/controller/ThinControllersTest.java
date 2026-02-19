package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.dto.article.CreateArticleRequest;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.service.ArticleCommentService;
import com.openclassrooms.mddapi.service.ArticleService;
import com.openclassrooms.mddapi.service.MddUserService;
import com.openclassrooms.mddapi.service.TopicService;
import com.openclassrooms.mddapi.service.UserTopicSubscriptionService;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

class ThinControllersTest {

    @Test
    void mddUserController_delegates() {
        MddUserService userService = Mockito.mock(MddUserService.class);
        MddUserController controller = new MddUserController(userService);

        Mockito.when(userService.getAll()).thenReturn(List.of(new UserDto(1L, "u", "e", "", "user", Instant.now())));
        Assertions.assertThat(controller.getAll()).hasSize(1);

        UserDto dto = new UserDto(2L, "u2", "e2", "", "user", Instant.now());
        Mockito.when(userService.getById(2L)).thenReturn(dto);
        Assertions.assertThat(controller.getById(2L)).isSameAs(dto);

        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);
        Mockito.when(userService.update(principal, dto)).thenReturn(dto);
        Assertions.assertThat(controller.updateCurrent(principal, dto)).isSameAs(dto);
    }

    @Test
    void topicController_delegates() {
        TopicService topicService = Mockito.mock(TopicService.class);
        TopicController controller = new TopicController(topicService);

        Mockito.when(topicService.getAll()).thenReturn(List.of(new TopicDto(1L, "t", "d")));
        Assertions.assertThat(controller.getAll()).hasSize(1);

        TopicDto dto = new TopicDto(2L, "t2", "d2");
        Mockito.when(topicService.getById(2L)).thenReturn(dto);
        Assertions.assertThat(controller.getById(2L)).isSameAs(dto);
    }

    @Test
    void articleController_delegates_and_wraps_create() {
        ArticleService articleService = Mockito.mock(ArticleService.class);
        ArticleController controller = new ArticleController(articleService);

        Mockito.when(articleService.getAll()).thenReturn(List.of());
        Assertions.assertThat(controller.getAll()).isEmpty();

        ArticleDto dto = new ArticleDto(1L, "t", "c", Instant.now(), 2L, 3L);
        Mockito.when(articleService.getById(1L)).thenReturn(dto);
        Assertions.assertThat(controller.getById(1L)).isSameAs(dto);

        MddUserEntity principal = new MddUserEntity();
        principal.setId(2L);
        CreateArticleRequest req = new CreateArticleRequest("t", "c", 3L);
        Mockito.when(articleService.create(principal, req)).thenReturn(dto);
        ResponseEntity<ArticleDto> created = controller.create(principal, req);
        Assertions.assertThat(created.getStatusCode().value()).isEqualTo(201);
        Assertions.assertThat(created.getBody()).isSameAs(dto);
    }

    @Test
    void articleCommentController_routes_by_articleId_and_wraps_create() {
        ArticleCommentService service = Mockito.mock(ArticleCommentService.class);
        ArticleCommentController controller = new ArticleCommentController(service);

        Mockito.when(service.getAllByArticleId(10L))
                .thenReturn(List.of(new CommentDto(1L, "c", Instant.now(), 2L, 10L)));
        Assertions.assertThat(controller.getAll(10L)).hasSize(1);

        MddUserEntity principal = new MddUserEntity();
        principal.setId(2L);

        CommentDto dto = new CommentDto(2L, "c2", Instant.now(), 2L, 10L);
        Mockito.when(service.create(principal, dto)).thenReturn(dto);
        ResponseEntity<CommentDto> created = controller.create(principal, dto);
        Assertions.assertThat(created.getStatusCode().value()).isEqualTo(201);
        Assertions.assertThat(created.getBody()).isSameAs(dto);
    }

    @Test
    void subscriptionController_delegates() {
        UserTopicSubscriptionService service = Mockito.mock(UserTopicSubscriptionService.class);
        UserTopicSubscriptionController controller = new UserTopicSubscriptionController(service);

        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Mockito.when(service.getByUser(principal)).thenReturn(List.of(new UserTopicSubscriptionDto(1L)));
        Assertions.assertThat(controller.getAll(principal)).hasSize(1);

        Mockito.when(service.subscribe(principal, 2L)).thenReturn(List.of(new UserTopicSubscriptionDto(2L)));
        Assertions.assertThat(controller.subscribe(principal, new UserTopicSubscriptionDto(2L))).hasSize(1);

        Mockito.when(service.unsubscribe(principal, 3L)).thenReturn(List.of());
        Assertions.assertThat(controller.unsubscribe(principal, 3L)).isEmpty();
    }
}
