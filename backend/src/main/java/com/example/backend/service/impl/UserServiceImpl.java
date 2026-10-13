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
import org.springframework.dao.DataIntegrityViolationException;

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
        String login = userLoginRequest.getLogin();
        
        if (login.contains("@")) {
            Optional<User> userOpt = userRepository.findByEmail(login);
            if (userOpt.isPresent() && passwordEncoder.matches(userLoginRequest.getPassword(), userOpt.get().getPassword())) {
                log.info("email and password Success for user: {}", login);
                return userOpt.get();
            }
        } else {
            Optional<User> userOpt = userRepository.findByUsername(login);
            if (userOpt.isPresent() && passwordEncoder.matches(userLoginRequest.getPassword(), userOpt.get().getPassword())) {
                log.info("username and password Success for user: {}", login);
                return userOpt.get();
            }
        }
        
        log.warn("Login failed: User not found or wrong password for login: {}", login);
        throw new WrongLoginCredentialsException();
    }

    public User createUser(RegisterRequestDto request) {
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
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
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyTakenException(request.getEmail());
            } else if (userRepository.existsByUsername(request.getUsername())) {
                throw new UsernameAlreadyTakenException(request.getUsername());
            }
            throw e;
        }
    }

    public User updateUser(Long id, com.example.backend.model.dto.UpdateUserRequestDto dto) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            Optional<User> byUsername = userRepository.findByUsername(dto.getUsername());
            if (byUsername.isPresent() && !byUsername.get().getId().equals(id)) {
                throw new UsernameAlreadyTakenException(dto.getUsername());
            }
            existingUser.setName(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (!dto.getEmail().contains("@")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            Optional<User> byEmail = userRepository.findByEmail(dto.getEmail());
            if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
                throw new EmailAlreadyTakenException(dto.getEmail());
            }
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), existingUser.getPassword())) {
                throw new IllegalArgumentException("Invalid old password");
            }
            String hashedPassword = passwordEncoder.encode(dto.getNewPassword());
            existingUser.setPassword(hashedPassword);
        }

        try {
            return userRepository.save(existingUser);
        } catch (DataIntegrityViolationException e) {
            if (dto.getEmail() != null) {
                Optional<User> byEmail = userRepository.findByEmail(dto.getEmail());
                if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
                    throw new EmailAlreadyTakenException(dto.getEmail());
                }
            }
            if (dto.getUsername() != null) {
                Optional<User> byUsername = userRepository.findByUsername(dto.getUsername());
                if (byUsername.isPresent() && !byUsername.get().getId().equals(id)) {
                    throw new UsernameAlreadyTakenException(dto.getUsername());
                }
            }
            throw e;
        }
    }

    public void deleteUser(Long id) {
        List<Comment> comments = commentRepository.findByAuthorIdOrderByTimestampDesc(id);
        commentRepository.deleteAll(comments);
        
        List<Post> posts = postRepository.findByAuthorIdOrderByTimestampDesc(id);
        postRepository.deleteAll(posts);
        
        userRepository.deleteById(id);
    }

}