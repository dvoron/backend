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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ForumServiceImpl forumService;

    @Test
    void getAllPostsReturnsListOfPostDtos() {
        User user = new User();
        user.setName("testuser");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setContent("Test Content");
        post.setAuthor(user);
        post.setTimestamp(LocalDateTime.now());
        post.setComments(new ArrayList<>());

        when(postRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(post));

        List<PostDto> result = forumService.getAllPosts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Post");
    }

    @Test
    void createPostWithValidUserReturnsPostDto() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");

        User user = new User();
        user.setId(1L);
        user.setName("testuser");

        Post savedPost = new Post();
        savedPost.setId(1L);
        savedPost.setTitle("New Post");
        savedPost.setContent("New Content");
        savedPost.setAuthor(user);
        savedPost.setTimestamp(LocalDateTime.now());
        savedPost.setComments(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostDto result = forumService.createPost(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Post");
        assertThat(result.getContent()).isEqualTo("New Content");
    }

    @Test
    void createPostWithInvalidUserThrowsException() {
        CreatePostRequest request = new CreatePostRequest();
        
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.createPost(request, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void createCommentWithValidUserAndPostReturnsCommentDto() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId(1L);
        request.setContent("New Comment");

        User user = new User();
        user.setId(1L);
        user.setName("testuser");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setContent("New Comment");
        savedComment.setAuthor(user);
        savedComment.setPost(post);
        savedComment.setTimestamp(LocalDateTime.now());
        savedComment.setReplies(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = forumService.createComment(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("New Comment");
    }

    @Test
    void createCommentWithParentCommentReturnsCommentDto() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId(1L);
        request.setParentCommentId(2L);
        request.setContent("Reply Comment");

        User user = new User();
        user.setId(1L);
        user.setName("testuser");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");

        Comment parentComment = new Comment();
        parentComment.setId(2L);

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setContent("Reply Comment");
        savedComment.setAuthor(user);
        savedComment.setPost(post);
        savedComment.setParentComment(parentComment);
        savedComment.setTimestamp(LocalDateTime.now());
        savedComment.setReplies(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = forumService.createComment(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Reply Comment");
    }

    @Test
    void createCommentWithInvalidUserThrowsException() {
        CreateCommentRequest request = new CreateCommentRequest();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.createComment(request, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void createCommentWithInvalidPostThrowsException() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId(99L);

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.createComment(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");
    }

    @Test
    void createCommentWithInvalidParentCommentThrowsException() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId(1L);
        request.setParentCommentId(99L);

        User user = new User();
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.createComment(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Parent comment not found");
    }

    @Test
    void getPostsByUserIdReturnsListOfPostDtos() {
        User user = new User();
        user.setName("testuser");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("User Post");
        post.setContent("User Content");
        post.setAuthor(user);
        post.setTimestamp(LocalDateTime.now());
        post.setComments(new ArrayList<>());

        when(postRepository.findByAuthorIdOrderByTimestampDesc(1L)).thenReturn(List.of(post));

        List<PostDto> result = forumService.getPostsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("User Post");
    }

    @Test
    void getCommentsByUserIdReturnsListOfCommentDtos() {
        User user = new User();
        user.setName("testuser");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("User Comment");
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setTimestamp(LocalDateTime.now());
        comment.setReplies(new ArrayList<>());

        when(commentRepository.findByAuthorIdOrderByTimestampDesc(1L)).thenReturn(List.of(comment));

        List<CommentDto> result = forumService.getCommentsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getContent()).isEqualTo("User Comment");
    }
}
