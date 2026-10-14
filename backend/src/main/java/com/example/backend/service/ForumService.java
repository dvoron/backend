package com.example.backend.service;

import com.example.backend.model.dto.CommentDto;
import com.example.backend.model.dto.CreateCommentRequest;
import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.PostDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ForumService {
    Page<PostDto> getAllPosts(int page, int size);
    PostDto createPost(CreatePostRequest request, Long userId);
    CommentDto createComment(CreateCommentRequest request, Long userId);
    List<PostDto> getPostsByUserId(Long userId);
    List<CommentDto> getCommentsByUserId(Long userId);
}
