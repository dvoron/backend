package com.example.backend.service.impl;

import com.example.backend.exception.EmailAlreadyTakenException;
import com.example.backend.exception.UsernameAlreadyTakenException;
import com.example.backend.exception.WrongLoginCredentialsException;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.dto.UpdateUserRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

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
    void signInWithValidUsernameButInvalidPasswordThrowsWrongLoginCredentialsException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLogin("testuser");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setName("testuser");
        user.setPassword("hashedPassword");

        when(userRepository.existsByEmail("testuser")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.signIn(request))
                .isInstanceOf(WrongLoginCredentialsException.class);
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

    @Test
    void createUserWithExistingUsernameThrowsUsernameAlreadyTakenException() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("new@example.com");
        request.setUsername("existinguser");
        request.setPassword("password");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UsernameAlreadyTakenException.class);
    }

    @Test
    void getAllUsersReturnsListOfUsers() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getUserByIdWithExistingIdReturnsUser() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserByIdWithNonExistingIdThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void signInWithNonExistingEmailOrUsernameThrowsWrongLoginCredentialsException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLogin("nonexisting");
        request.setPassword("password");

        when(userRepository.existsByEmail("nonexisting")).thenReturn(false);
        when(userRepository.existsByUsername("nonexisting")).thenReturn(false);

        assertThatThrownBy(() -> userService.signIn(request))
                .isInstanceOf(WrongLoginCredentialsException.class);
    }

    @Test
    void updateUserWithValidDataUpdatesAndReturnsUser() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("olduser");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("oldHashedPassword");

        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setOldPassword("oldpassword");
        dto.setNewPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldpassword", "oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newpassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateUser(1L, dto);

        assertThat(result.getName()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getPassword()).isEqualTo("newHashedPassword");
    }

    @Test
    void updateUserWithInvalidOldPasswordThrowsException() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPassword("oldHashedPassword");

        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        dto.setOldPassword("wrongoldpassword");
        dto.setNewPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongoldpassword", "oldHashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUser(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid old password");
    }

    @Test
    void updateUserWithNonExistingIdThrowsException() {
        UpdateUserRequestDto dto = new UpdateUserRequestDto();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteUserCallsRepositoryDeleteById() {
        when(commentRepository.findByAuthorIdOrderByTimestampDesc(1L)).thenReturn(List.of());
        when(postRepository.findByAuthorIdOrderByTimestampDesc(1L)).thenReturn(List.of());

        userService.deleteUser(1L);

        verify(commentRepository, times(1)).deleteAll(List.of());
        verify(postRepository, times(1)).deleteAll(List.of());
        verify(userRepository, times(1)).deleteById(1L);
    }
}
