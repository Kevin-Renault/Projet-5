package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.exception.ApiErrorResponse;
import com.openclassrooms.mddapi.service.MddUserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Create and retrieve users, and update the current authenticated user.")
public class MddUserController {

    private final MddUserService userService;

    public MddUserController(MddUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List users", description = "Returns all users.", responses = {
            @ApiResponse(responseCode = "200", description = "Users returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Returns a single user.", responses = {
            @ApiResponse(responseCode = "200", description = "User returned", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid id", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserDto getById(
            @Parameter(description = "User id", example = "1") @PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user.", responses = {
            @ApiResponse(responseCode = "201", description = "User created", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "User payload", content = @Content(schema = @Schema(implementation = UserDto.class))) @Valid @RequestBody UserDto request) {
        UserDto created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    @Operation(summary = "Update current user", description = "Updates the authenticated user's profile.", responses = {
            @ApiResponse(responseCode = "200", description = "User updated", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserDto updateCurrent(
            @Parameter(hidden = true) @AuthenticationPrincipal MddUserEntity principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Updated user payload", content = @Content(schema = @Schema(implementation = UserDto.class))) @Valid @RequestBody UserDto request) {
        return userService.update(principal, request);
    }
}
