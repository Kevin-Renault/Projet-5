package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.service.UserTopicSubscriptionService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/subscriptions")
public class UserTopicSubscriptionController {

    private final UserTopicSubscriptionService subscriptionService;

    public UserTopicSubscriptionController(UserTopicSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public List<UserTopicSubscriptionDto> getAll(
            @AuthenticationPrincipal MddUserEntity principal) {
        return subscriptionService.getByUser(principal);
    }

    @PostMapping
    public List<UserTopicSubscriptionDto> subscribe(
            @AuthenticationPrincipal MddUserEntity principal,
            @Valid @RequestBody UserTopicSubscriptionDto request) {

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }
        return subscriptionService.subscribe(principal, request.topicId());
    }

    @DeleteMapping
    public List<UserTopicSubscriptionDto> unsubscribe(
            @AuthenticationPrincipal MddUserEntity principal,
            @RequestParam Long topicId) {
        if (topicId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }
        return subscriptionService.unsubscribe(principal, topicId);
    }
}
