package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.exception.ApiErrorResponse;
import com.openclassrooms.mddapi.service.ArticleCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Comments", description = "Create and retrieve article comments.")
@SecurityRequirement(name = com.openclassrooms.mddapi.config.OpenApiConfig.BEARER_AUTH_SCHEME)
@SecurityRequirement(name = com.openclassrooms.mddapi.config.OpenApiConfig.COOKIE_AUTH_SCHEME)
public class ArticleCommentController {

    private final ArticleCommentService commentService;

    public ArticleCommentController(ArticleCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "List comments", description = "Returns all comments. If 'articleId' is provided, returns only comments for the given article.", responses = {
            @ApiResponse(responseCode = "200", description = "Comments returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CommentDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<CommentDto> getAll(
            @Parameter(description = "Filter comments by article id", example = "1") @RequestParam(required = false) Long articleId) {
        if (articleId != null) {
            return commentService.getAllByArticleId(articleId);
        }
        return commentService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by id", description = "Returns a single comment.", responses = {

            @ApiResponse(responseCode = "200", description = "Comment returned", content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid id", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public CommentDto getById(
            @Parameter(description = "Comment id", example = "1") @PathVariable Long id) {
        return commentService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create comment", description = "Creates a new comment for the authenticated user.", responses = {
            @ApiResponse(responseCode = "201", description = "Comment created", content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Related resource not found (e.g. article)", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CommentDto> create(
            @Parameter(hidden = true) @AuthenticationPrincipal MddUserEntity principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Comment payload (must contain 'content' and 'articleId')", content = @Content(schema = @Schema(implementation = CommentDto.class))) @Valid @RequestBody CommentDto request) {
        CommentDto created = commentService.create(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
