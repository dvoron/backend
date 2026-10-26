package com.example.backend.service;

import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.dto.CommentDto;
import com.example.backend.model.dto.CreateCommentRequest;
import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.PostDto;
import com.example.backend.model.entity.Comment;
import com.example.backend.model.entity.Post;
import com.example.backend.model.entity.User;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public ForumService(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PostDto> getAllPosts(Long cursor, int size) {
        List<Post> posts;
        if (cursor == null) {
            posts = postRepository.findAllByOrderByIdDesc(PageRequest.of(0, size));
        } else {
            posts = postRepository.findByIdLessThanOrderByIdDesc(cursor, PageRequest.of(0, size));
        }
        return posts.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public PostDto createPost(CreatePostRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(user);
        post.setTimestamp(Instant.now());

        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    @Transactional
    public CommentDto createComment(CreateCommentRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            
            if (parentComment.getPost() == null || !parentComment.getPost().getId().equals(request.getPostId())) {
                throw new IllegalArgumentException("Parent comment does not belong to the specified post");
            }
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setParentComment(parentComment);
        comment.setTimestamp(Instant.now());

        Comment saved = commentRepository.save(comment);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PostDto> getPostsByUserId(Long userId) {
        List<Post> posts = postRepository.findByAuthorIdOrderByTimestampDesc(userId);
        return posts.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentRepository.findByAuthorIdOrderByTimestampDesc(userId);
        return comments.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private PostDto mapToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        
        if (post.getAuthor() != null) {
            dto.setAuthor(post.getAuthor().getUsername());
        } else {
            dto.setAuthor("[deleted user]");
        }
        
        dto.setTimestamp(post.getTimestamp());

        List<CommentDto> comments = post.getComments().stream()
                .filter(c -> c.getParentComment() == null) // only top level
                .map(c -> mapToDto(c, 5))
                .collect(Collectors.toList());
        dto.setComments(comments);

        return dto;
    }

    private CommentDto mapToDto(Comment comment) {
        return mapToDto(comment, 0);
    }

    private CommentDto mapToDto(Comment comment, int depth) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        
        if (comment.getAuthor() != null) {
            dto.setAuthor(comment.getAuthor().getUsername());
        } else {
            dto.setAuthor("[deleted user]");
        }
        
        dto.setTimestamp(comment.getTimestamp());

        if (comment.getPost() != null) {
            dto.setPostTitle(comment.getPost().getTitle());
            dto.setPostId(comment.getPost().getId());
        }

        if (depth > 0) {
            List<CommentDto> replies = comment.getReplies().stream()
                    .map(c -> mapToDto(c, depth - 1))
                    .collect(Collectors.toList());
            dto.setReplies(replies);
            dto.setReplyCount(replies.size());
        } else {
            dto.setReplyCount(comment.getReplies() != null ? comment.getReplies().size() : 0);
            dto.setReplies(new java.util.ArrayList<>());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getRepliesForComment(Long commentId) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        return parentComment.getReplies().stream()
                .map(c -> mapToDto(c, 5))
                .collect(Collectors.toList());
    }
}
