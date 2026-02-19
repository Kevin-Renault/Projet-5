package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.entity.ArticleCommentEntity;
import com.openclassrooms.mddapi.entity.ArticleEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.mapper.CommentMapper;
import com.openclassrooms.mddapi.repository.ArticleCommentRepository;
import com.openclassrooms.mddapi.repository.ArticleRepository;
import com.openclassrooms.mddapi.repository.MddUserRepository;
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
class ArticleCommentServiceTest {

    @Mock
    private ArticleCommentRepository commentRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private MddUserRepository userRepository;

    @Mock
    private CommentMapper mapper;

    @InjectMocks
    private ArticleCommentService service;

    @Test
    void getAllByArticleId_requires_articleId() {
        Assertions.assertThatThrownBy(() -> service.getAllByArticleId(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void create_requires_auth_and_valid_payload() {
        Assertions.assertThatThrownBy(() -> service.create(null, new CommentDto(null, "c", null, null, 1L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Assertions.assertThatThrownBy(() -> service.create(principal, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void create_requires_articleId_and_content() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Assertions.assertThatThrownBy(() -> service.create(principal, new CommentDto(null, "c", null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        Assertions.assertThatThrownBy(() -> service.create(principal, new CommentDto(null, "   ", null, null, 1L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void create_unknown_article_throws_404() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Mockito.when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> service.create(principal, new CommentDto(null, "c", null, null, 99L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void create_happy_path_trims_and_maps() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        ArticleEntity article = new ArticleEntity();
        article.setId(2L);
        Mockito.when(articleRepository.findById(2L)).thenReturn(Optional.of(article));

        MddUserEntity authorRef = new MddUserEntity();
        authorRef.setId(1L);
        Mockito.when(userRepository.getReferenceById(1L)).thenReturn(authorRef);

        ArticleCommentEntity saved = new ArticleCommentEntity();
        saved.setId(10L);

        ArgumentCaptor<ArticleCommentEntity> entityCaptor = ArgumentCaptor.forClass(ArticleCommentEntity.class);
        Mockito.when(commentRepository.save(entityCaptor.capture())).thenReturn(saved);

        CommentDto dto = new CommentDto(10L, "C", null, 1L, 2L);
        Mockito.when(mapper.toDto(saved)).thenReturn(dto);

        CommentDto out = service.create(principal, new CommentDto(null, "  C  ", null, null, 2L));
        Assertions.assertThat(out).isSameAs(dto);

        ArticleCommentEntity toSave = entityCaptor.getValue();
        Assertions.assertThat(toSave.getContent()).isEqualTo("C");
        Assertions.assertThat(toSave.getAuthor()).isSameAs(authorRef);
        Assertions.assertThat(toSave.getArticle()).isSameAs(article);
    }

    @Test
    void update_checks_owner_and_handles_blank_content() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        ArticleCommentEntity existing = new ArticleCommentEntity();
        existing.setId(5L);
        MddUserEntity author = new MddUserEntity();
        author.setId(1L);
        existing.setAuthor(author);
        existing.setContent("old");

        Mockito.when(commentRepository.findById(5L)).thenReturn(Optional.of(existing));

        // blank -> no content update, still saves
        Mockito.when(commentRepository.save(existing)).thenReturn(existing);
        Mockito.when(mapper.toDto(existing)).thenReturn(new CommentDto(5L, "old", null, 1L, 1L));

        CommentDto out = service.update(5L, principal, new CommentDto(null, "   ", null, null, 1L));
        Assertions.assertThat(out.content()).isEqualTo("old");

        // not owner -> 403
        MddUserEntity otherPrincipal = new MddUserEntity();
        otherPrincipal.setId(2L);
        Assertions
                .assertThatThrownBy(() -> service.update(5L, otherPrincipal, new CommentDto(null, "x", null, null, 1L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void delete_checks_owner() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        ArticleCommentEntity existing = new ArticleCommentEntity();
        existing.setId(5L);
        MddUserEntity author = new MddUserEntity();
        author.setId(2L);
        existing.setAuthor(author);

        Mockito.when(commentRepository.findById(5L)).thenReturn(Optional.of(existing));

        Assertions.assertThatThrownBy(() -> service.delete(5L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");

        principal.setId(2L);
        Assertions.assertThatCode(() -> service.delete(5L, principal)).doesNotThrowAnyException();
        Mockito.verify(commentRepository).delete(existing);
    }
}
