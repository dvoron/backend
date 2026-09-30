package com.example.backend.service;

import com.example.backend.model.dto.CommentDto;
import com.example.backend.model.dto.CreateCommentRequest;
import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.PostDto;

import java.util.List;

public interface ForumService {
    List<PostDto> getAllPosts();
    PostDto createPost(CreatePostRequest request, Long userId);
    CommentDto createComment(CreateCommentRequest request, Long userId);
    List<PostDto> getPostsByUserId(Long userId);
    List<CommentDto> getCommentsByUserId(Long userId);
}
