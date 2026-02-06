package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.article.CreateArticleRequest;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.service.ArticleService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleDto> getAll() {
        return articleService.getAll();
    }

    @GetMapping("/{id}")
    public ArticleDto getById(@PathVariable Long id) {
        return articleService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ArticleDto> create(
            @AuthenticationPrincipal MddUserEntity principal,
            @Valid @RequestBody CreateArticleRequest request) {

        ArticleDto created = articleService.create(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
