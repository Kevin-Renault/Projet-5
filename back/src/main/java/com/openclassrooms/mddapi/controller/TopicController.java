package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.exception.ApiErrorResponse;
import com.openclassrooms.mddapi.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topics")
@Tag(name = "Topics", description = "Retrieve available topics.")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    @Operation(summary = "List topics", description = "Returns all topics.", responses = {
            @ApiResponse(responseCode = "200", description = "Topics returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopicDto.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public List<TopicDto> getAll() {
        return topicService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get topic by id", description = "Returns a single topic.", responses = {
            @ApiResponse(responseCode = "200", description = "Topic returned", content = @Content(schema = @Schema(implementation = TopicDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid id", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public TopicDto getById(
            @Parameter(description = "Topic id", example = "1") @PathVariable Long id) {
        return topicService.getById(id);
    }
}
