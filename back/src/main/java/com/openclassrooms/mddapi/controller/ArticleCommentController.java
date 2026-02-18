package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.service.ArticleCommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class ArticleCommentController {

    private final ArticleCommentService commentService;

    public ArticleCommentController(ArticleCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentDto> getAll(@RequestParam(required = false) Long articleId) {
        if (articleId != null) {
            return commentService.getAllByArticleId(articleId);
        }
        return commentService.getAll();
    }

    @GetMapping("/{id}")
    public CommentDto getById(@PathVariable Long id) {
        return commentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CommentDto> create(
            @AuthenticationPrincipal MddUserEntity principal,
            @Valid @RequestBody CommentDto request) {
        CommentDto created = commentService.create(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
