package com.example.backend.repository;

import com.example.backend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailWithExistingEmailReturnsUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("testuser");
        user.setPassword("password");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmailWithNonExistingEmailReturnsEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexisting@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmailWithExistingEmailReturnsTrue() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("testuser");
        user.setPassword("password");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailWithNonExistingEmailReturnsFalse() {
        boolean exists = userRepository.existsByEmail("nonexisting@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void findByUsernameWithExistingUsernameReturnsUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("testuser");
        user.setPassword("password");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("testuser");
    }

    @Test
    void existsByUsernameWithExistingUsernameReturnsTrue() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("testuser");
        user.setPassword("password");
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("testuser");

        assertThat(exists).isTrue();
    }
}
