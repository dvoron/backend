package com.example.backend.controller;

import com.example.backend.model.dto.CommentDto;
import com.example.backend.model.dto.CreateCommentRequest;
import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.PostDto;
import com.example.backend.service.ForumService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        List<PostDto> posts = forumService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody CreatePostRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PostDto created = forumService.createPost(request, userId);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentDto> createComment(@RequestBody CreateCommentRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CommentDto created = forumService.createComment(request, userId);
        return ResponseEntity.ok(created);
    }
}
