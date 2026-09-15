package com.example.backend.service.impl;

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
import com.example.backend.service.ForumService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl implements ForumService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy, h:mm:ss a");

    public ForumServiceImpl(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByTimestampDesc();
        return posts.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostDto createPost(CreatePostRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(user);
        post.setTimestamp(LocalDateTime.now());

        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public CommentDto createComment(CreateCommentRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setParentComment(parentComment);
        comment.setTimestamp(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getPostsByUserId(Long userId) {
        List<Post> posts = postRepository.findByAuthorIdOrderByTimestampDesc(userId);
        return posts.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentRepository.findByAuthorIdOrderByTimestampDesc(userId);
        return comments.stream().map(this::mapToDtoWithPostInfo).collect(Collectors.toList());
    }

    private PostDto mapToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthor(post.getAuthor().getName());
        dto.setTimestamp(post.getTimestamp().format(FORMATTER));
        dto.setShowComments(false);
        dto.setNewCommentContent("");

        List<CommentDto> comments = post.getComments().stream()
                .filter(c -> c.getParentComment() == null) // only top level
                .map(this::mapToDto)
                .collect(Collectors.toList());
        dto.setComments(comments);

        return dto;
    }

    private CommentDto mapToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthor(comment.getAuthor().getName());
        dto.setTimestamp(comment.getTimestamp().format(FORMATTER));

        if (comment.getPost() != null) {
            dto.setPostTitle(comment.getPost().getTitle());
            dto.setPostId(comment.getPost().getId());
        }

        List<CommentDto> replies = comment.getReplies().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        dto.setReplies(replies);

        return dto;
    }

    private CommentDto mapToDtoWithPostInfo(Comment comment) {
        CommentDto dto = mapToDto(comment);
        // Post info is already set in mapToDto if comment.getPost() is not null
        // But for replies, getPost() might be null depending on how the entity is mapped.
        // Let's ensure post info is set by traversing up if needed.
        Post post = comment.getPost();
        Comment current = comment;
        while (post == null && current.getParentComment() != null) {
            current = current.getParentComment();
            post = current.getPost();
        }
        
        if (post != null) {
            dto.setPostTitle(post.getTitle());
            dto.setPostId(post.getId());
        }
        
        return dto;
    }
}
