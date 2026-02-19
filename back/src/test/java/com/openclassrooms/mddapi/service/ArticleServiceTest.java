package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.article.CreateArticleRequest;
import com.openclassrooms.mddapi.entity.ArticleEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.mapper.ArticleMapper;
import com.openclassrooms.mddapi.repository.ArticleRepository;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private MddUserRepository userRepository;

    @Mock
    private ArticleMapper mapper;

    @InjectMocks
    private ArticleService service;

    @Test
    void getAll_sorts_and_maps() {
        ArticleEntity entity = new ArticleEntity();
        entity.setId(1L);

        Mockito.when(articleRepository.findAll(Mockito.any(Sort.class))).thenReturn(List.of(entity));

        ArticleDto dto = new ArticleDto(1L, "t", "c", null, null, null);
        Mockito.when(mapper.toDto(entity)).thenReturn(dto);

        List<ArticleDto> out = service.getAll();
        Assertions.assertThat(out).containsExactly(dto);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        Mockito.verify(articleRepository).findAll(sortCaptor.capture());
        Assertions.assertThat(sortCaptor.getValue()).isNotNull();
    }

    @Test
    void getById_not_found_throws_404() {
        Mockito.when(articleRepository.findById(9L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> service.getById(9L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void create_requires_auth_and_valid_payload() {
        CreateArticleRequest unauthRequest = new CreateArticleRequest("t", "c", 1L);
        Assertions.assertThatThrownBy(() -> service.create(null, unauthRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Assertions.assertThatThrownBy(() -> service.create(principal, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void create_happy_path_trims_and_maps() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        TopicEntity topic = new TopicEntity();
        topic.setId(2L);
        Mockito.when(topicRepository.findById(2L)).thenReturn(Optional.of(topic));

        MddUserEntity authorRef = new MddUserEntity();
        authorRef.setId(1L);
        Mockito.when(userRepository.getReferenceById(1L)).thenReturn(authorRef);

        ArticleEntity saved = new ArticleEntity();
        saved.setId(10L);

        ArgumentCaptor<ArticleEntity> entityCaptor = ArgumentCaptor.forClass(ArticleEntity.class);
        Mockito.when(articleRepository.save(entityCaptor.capture())).thenReturn(saved);

        ArticleDto dto = new ArticleDto(10L, "T", "C", null, 1L, 2L);
        Mockito.when(mapper.toDto(saved)).thenReturn(dto);

        ArticleDto out = service.create(principal, new CreateArticleRequest("  T  ", "  C  ", 2L));
        Assertions.assertThat(out).isSameAs(dto);

        ArticleEntity toSave = entityCaptor.getValue();
        Assertions.assertThat(toSave.getTitle()).isEqualTo("T");
        Assertions.assertThat(toSave.getContent()).isEqualTo("C");
        Assertions.assertThat(toSave.getAuthor()).isSameAs(authorRef);
        Assertions.assertThat(toSave.getTopic()).isSameAs(topic);
    }

    @Test
    void create_unknown_topic_throws_404() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Mockito.when(topicRepository.findById(99L)).thenReturn(Optional.empty());
        CreateArticleRequest request = new CreateArticleRequest("t", "c", 99L);
        Assertions.assertThatThrownBy(() -> service.create(principal, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
