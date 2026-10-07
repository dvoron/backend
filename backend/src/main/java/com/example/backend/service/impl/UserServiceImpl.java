package com.example.backend.service.impl;



import com.example.backend.exception.EmailAlreadyTakenException;
import com.example.backend.exception.UsernameAlreadyTakenException;
import com.example.backend.exception.WrongLoginCredentialsException;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.CommentRepository;
import com.example.backend.model.entity.Post;
import com.example.backend.model.entity.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    public User signIn(LoginRequestDto userLoginRequest) {
        if (userRepository.existsByEmail(userLoginRequest.getLogin())) {
            Optional<User> user = userRepository.findByEmail(userLoginRequest.getLogin());
            if (passwordEncoder.matches(userLoginRequest.getPassword(), user.get().getPassword())) {
                log.info("email and password Success for user: {}", userLoginRequest.getLogin());
                return user.get();
            } else {
                log.warn("email and password Failure for user: {}", userLoginRequest.getLogin());
                throw new WrongLoginCredentialsException();
            }
        } else if (userRepository.existsByUsername(userLoginRequest.getLogin())) {
            Optional<User> user = userRepository.findByUsername(userLoginRequest.getLogin());
            if (passwordEncoder.matches(userLoginRequest.getPassword(), user.get().getPassword())) {
                log.info("username and password Success for user: {}", userLoginRequest.getLogin());
                return user.get();
            } else {
                log.warn("username and password Failure for user: {}", userLoginRequest.getLogin());
                throw new WrongLoginCredentialsException();
            }
        }
        log.warn("Login failed: User not found for login: {}", userLoginRequest.getLogin());
        throw new WrongLoginCredentialsException();
    }

    public User createUser(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyTakenException(request.getEmail());
        } else if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyTakenException(request.getUsername());
        }
        User user = new User();
        user.setName(request.getUsername());
        user.setEmail(request.getEmail());
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public User updateUser(Long id, com.example.backend.model.dto.UpdateUserRequestDto dto) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            existingUser.setName(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), existingUser.getPassword())) {
                throw new IllegalArgumentException("Invalid old password");
            }
            String hashedPassword = passwordEncoder.encode(dto.getNewPassword());
            existingUser.setPassword(hashedPassword);
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        List<Comment> comments = commentRepository.findByAuthorIdOrderByTimestampDesc(id);
        commentRepository.deleteAll(comments);
        
        List<Post> posts = postRepository.findByAuthorIdOrderByTimestampDesc(id);
        postRepository.deleteAll(posts);
        
        userRepository.deleteById(id);
    }

}