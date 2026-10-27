package com.example.backend.service;



import com.example.backend.exception.AccountLockedException;
import com.example.backend.exception.EmailAlreadyTakenException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UsernameAlreadyTakenException;
import com.example.backend.exception.WrongLoginCredentialsException;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.dto.UpdateUserRequestDto;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;



@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final LoginAttemptService loginAttemptService;
    private final String dummyPasswordHash;

    public UserService(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder, SessionService sessionService, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.loginAttemptService = loginAttemptService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing-attack-prevention");
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    public User signIn(LoginRequestDto userLoginRequest) {
        String login = userLoginRequest.getLogin().toLowerCase();
        
        Optional<User> userOpt;
        if (login.contains("@")) {
            userOpt = userRepository.findByEmail(login);
        } else {
            userOpt = userRepository.findByUsername(login);
        }

        String attemptKey = userOpt.isPresent() ? userOpt.get().getUsername() : login;

        if (loginAttemptService.isBlocked(attemptKey)) {
            log.warn("Login attempt blocked for user/key: {}", attemptKey);
            throw new AccountLockedException("Account locked due to too many failed login attempts");
        }

        if (userOpt.isPresent()) {
            if (passwordEncoder.matches(userLoginRequest.getPassword(), userOpt.get().getPassword())) {
                log.info("Success for user: {}", userOpt.get().getUsername());
                loginAttemptService.loginSucceeded(userOpt.get().getUsername());
                return userOpt.get();
            }
        } else {
            // Mitigate timing attack
            passwordEncoder.matches(userLoginRequest.getPassword(), dummyPasswordHash);
        }
        
        log.warn("Login failed: User not found or wrong password for login: {}", login);
        if (userOpt.isPresent()) {
            loginAttemptService.loginFailed(userOpt.get().getUsername());
        } else {
            loginAttemptService.loginFailed(login);
        }
        throw new WrongLoginCredentialsException();
    }

    public User createUser(RegisterRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        request.setEmail(email);

        if (request.getUsername() != null) {
            String trimmedUsername = request.getUsername().trim();
            if (trimmedUsername.contains("@")) {
                throw new IllegalArgumentException("Username cannot contain '@'");
            }
            request.setUsername(trimmedUsername.toLowerCase());
        }
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyTakenException(request.getEmail());
        } else if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyTakenException(request.getUsername());
        }
        User user = new User();
        user.setUsername(request.getUsername());
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

    @Transactional
    public User updateUser(Long id, UpdateUserRequestDto dto) {
        if (dto.getEmail() != null) {
            dto.setEmail(dto.getEmail().toLowerCase());
        }
        if (dto.getUsername() != null) {
            dto.setUsername(dto.getUsername().toLowerCase());
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        boolean isEmailChanged = dto.getEmail() != null && !dto.getEmail().isEmpty() && !dto.getEmail().equals(existingUser.getEmail());
        
        if (dto.getNewPassword() != null && dto.getNewPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        boolean isPasswordChanged = dto.getNewPassword() != null && !dto.getNewPassword().isEmpty();
        
        if (isEmailChanged) {
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), existingUser.getPassword())) {
                throw new IllegalArgumentException("Current password is required to change email");
            }
        }

        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            String trimmedUsername = dto.getUsername().trim();
            if (trimmedUsername.contains("@")) {
                throw new IllegalArgumentException("Username cannot contain '@'");
            }
            Optional<User> byUsername = userRepository.findByUsername(trimmedUsername);
            if (byUsername.isPresent() && !byUsername.get().getId().equals(id)) {
                throw new UsernameAlreadyTakenException(trimmedUsername);
            }
            existingUser.setUsername(trimmedUsername);
        }
        if (isEmailChanged) {
            Optional<User> byEmail = userRepository.findByEmail(dto.getEmail());
            if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
                throw new EmailAlreadyTakenException(dto.getEmail());
            }
            existingUser.setEmail(dto.getEmail());
        }
        if (isPasswordChanged) {
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), existingUser.getPassword())) {
                throw new IllegalArgumentException("Invalid old password");
            }
            String hashedPassword = passwordEncoder.encode(dto.getNewPassword());
            existingUser.setPassword(hashedPassword);
        }

        try {
            User savedUser = userRepository.save(existingUser);
            if (isPasswordChanged) {
                sessionService.revokeAllUserSessions(id);
            }
            return savedUser;
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

    @Transactional
    public void deleteUser(Long id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        sessionService.deleteAllUserSessions(id);
        
        // Anonymise user's posts and comments instead of deleting them
        List<Post> posts = postRepository.findByAuthorId(id);
        for (Post post : posts) {
            post.setAuthor(null);
        }
        postRepository.saveAll(posts);

        List<Comment> comments = commentRepository.findByAuthorId(id);
        for (Comment comment : comments) {
            comment.setAuthor(null);
        }
        commentRepository.saveAll(comments);
        
        userRepository.deleteById(id);
    }

}
