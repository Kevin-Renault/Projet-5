package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.entity.ArticleCommentEntity;
import com.openclassrooms.mddapi.entity.ArticleEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.mapper.CommentMapper;
import com.openclassrooms.mddapi.repository.ArticleCommentRepository;
import com.openclassrooms.mddapi.repository.ArticleRepository;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ArticleCommentService {

    private static final String ARTICLE_NOT_FOUND = "Article not found";

    private final ArticleCommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MddUserRepository userRepository;
    private final CommentMapper commentMapper;

    public ArticleCommentService(
            ArticleCommentRepository commentRepository,
            ArticleRepository articleRepository,
            MddUserRepository userRepository,
            CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getAllByArticleId(Long articleId) {
        if (articleId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "articleId is required");
        }
        return commentRepository.findAllByArticle_IdOrderByCreatedAtAsc(articleId)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Transactional
    public CommentDto create(MddUserEntity principal, CommentDto request) {
        Long principalId = requireAuthenticatedUserId(principal);

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }

        String content = trimToNull(request.content());
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }

        if (request.articleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Article ID is required");
        }

        ArticleEntity article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ARTICLE_NOT_FOUND));

        ArticleCommentEntity entity = new ArticleCommentEntity();
        entity.setContent(content);
        entity.setArticle(article);
        entity.setAuthor(userRepository.getReferenceById(principalId));

        ArticleCommentEntity saved = commentRepository.save(entity);
        return commentMapper.toDto(saved);
    }

    private static Long requireAuthenticatedUserId(MddUserEntity principal) {
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal.getId();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
