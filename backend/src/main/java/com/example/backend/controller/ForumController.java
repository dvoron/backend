package com.example.backend.controller;

import com.example.backend.model.dto.CommentDto;
import com.example.backend.model.dto.CreateCommentRequest;
import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.PostDto;
import com.example.backend.service.ForumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Forum", description = "Endpoints for managing posts and comments")
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @Operation(summary = "Get all posts", description = "Retrieves a paginated list of all posts")
    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int cappedSize = Math.min(Math.max(1, size), 50);
        Page<PostDto> posts = forumService.getAllPosts(page, cappedSize);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Create post", description = "Creates a new post authored by the authenticated user")
    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody CreatePostRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PostDto created = forumService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Create comment", description = "Creates a new comment on a post or a reply to an existing comment")
    @PostMapping("/comments")
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CreateCommentRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CommentDto created = forumService.createComment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get user posts", description = "Retrieves a list of posts authored by a specific user")
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<PostDto>> getPostsByUserId(@PathVariable Long userId) {
        List<PostDto> posts = forumService.getPostsByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Get user comments", description = "Retrieves a list of comments authored by a specific user")
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByUserId(@PathVariable Long userId) {
        List<CommentDto> comments = forumService.getCommentsByUserId(userId);
        return ResponseEntity.ok(comments);
    }
}
