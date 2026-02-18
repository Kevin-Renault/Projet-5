package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.exception.ApiErrorResponse;
import com.openclassrooms.mddapi.service.UserTopicSubscriptionService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "Manage the authenticated user's topic subscriptions.")
public class UserTopicSubscriptionController {

    private final UserTopicSubscriptionService subscriptionService;

    public UserTopicSubscriptionController(UserTopicSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    @Operation(summary = "List my subscriptions", description = "Returns the topics the authenticated user is subscribed to.", responses = {
            @ApiResponse(responseCode = "200", description = "Subscriptions returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserTopicSubscriptionDto.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<UserTopicSubscriptionDto> getAll(
            @Parameter(hidden = true) @AuthenticationPrincipal MddUserEntity principal) {
        return subscriptionService.getByUser(principal);
    }

    @PostMapping
    @Operation(summary = "Subscribe to a topic", description = "Subscribes the authenticated user to a topic.", responses = {
            @ApiResponse(responseCode = "200", description = "Subscription updated", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserTopicSubscriptionDto.class)))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<UserTopicSubscriptionDto> subscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal MddUserEntity principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Subscription payload", content = @Content(schema = @Schema(implementation = UserTopicSubscriptionDto.class))) @Valid @RequestBody UserTopicSubscriptionDto request) {
        return subscriptionService.subscribe(principal, request.topicId());
    }

    @DeleteMapping
    @Operation(summary = "Unsubscribe from a topic", description = "Unsubscribes the authenticated user from a topic.", responses = {
            @ApiResponse(responseCode = "200", description = "Subscription updated", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserTopicSubscriptionDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<UserTopicSubscriptionDto> unsubscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal MddUserEntity principal,
            @Parameter(description = "Topic id", example = "1") @RequestParam Long topicId) {
        return subscriptionService.unsubscribe(principal, topicId);
    }
}
