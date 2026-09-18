package com.example.backend.service.impl;

import com.example.backend.exception.EmailAlreadyTakenException;
import com.example.backend.exception.UsernameAlreadyTakenException;
import com.example.backend.exception.WrongLoginCredentialsException;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.dto.UpdateUserRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void signInWithValidEmailReturnsUser() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLogin("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = userService.signIn(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void signInWithValidUsernameReturnsUser() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLogin("testuser");
        request.setPassword("password");

        User user = new User();
        user.setName("testuser");
        user.setPassword("hashedPassword");

        when(userRepository.existsByEmail("testuser")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = userService.signIn(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("testuser");
    }

    @Test
    void signInWithInvalidPasswordThrowsWrongLoginCredentialsException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLogin("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.signIn(request))
                .isInstanceOf(WrongLoginCredentialsException.class);
    }

    @Test
    void createUserWithNewEmailAndUsernameReturnsSavedUser() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("new@example.com");
        request.setUsername("newuser");
        request.setPassword("password");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setEmail("new@example.com");
        savedUser.setName("newuser");
        savedUser.setPassword("hashedPassword");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getName()).isEqualTo("newuser");
        assertThat(result.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    void createUserWithExistingEmailThrowsEmailAlreadyTakenException() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("existing@example.com");
        request.setUsername("newuser");
        request.setPassword("password");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyTakenException.class);
    }
}
