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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ArticleCommentService {

    private static final String COMMENT_NOT_FOUND = "Comment not found";
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
    public List<CommentDto> getAll() {
        return commentRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CommentDto getById(Long id) {
        ArticleCommentEntity comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND));
        return commentMapper.toDto(comment);
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

    @Transactional
    public CommentDto update(Long id, MddUserEntity principal, CommentDto request) {
        Long principalId = requireAuthenticatedUserId(principal);

        ArticleCommentEntity comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND));

        Long authorId = comment.getAuthor() != null ? comment.getAuthor().getId() : null;
        if (authorId == null || !authorId.equals(principalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }

        String content = trimToNull(request.content());
        if (content != null) {
            comment.setContent(content);
        }

        ArticleCommentEntity saved = commentRepository.save(comment);
        return commentMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id, MddUserEntity principal) {
        Long principalId = requireAuthenticatedUserId(principal);

        ArticleCommentEntity comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND));

        Long authorId = comment.getAuthor() != null ? comment.getAuthor().getId() : null;
        if (authorId == null || !authorId.equals(principalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        commentRepository.delete(comment);
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
