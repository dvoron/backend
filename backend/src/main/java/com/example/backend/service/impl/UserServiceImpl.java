package com.example.backend.service.impl;



import com.example.backend.exception.EmailAlreadyTakenException;
import com.example.backend.exception.UsernameAlreadyTakenException;
import com.example.backend.exception.WrongLoginCredentialsException;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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
//        Authentication authentication =
//                authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(
//                                request.username(),
//                                request.password()
//                        )
//                );
//        boolean passwordMatchesHash = passwordEncoder.matches()
        if (userRepository.existsByEmail(userLoginRequest.getLogin())) {
            Optional<User> user = userRepository.findByEmail(userLoginRequest.getLogin());
            if (passwordEncoder.matches(userLoginRequest.getPassword(), user.get().getPassword())) {
//            if (userLoginRequest.getPassword().equals(user.get().getPassword())) {
                System.out.println("email and password Success");
                return user.get();
            } else {
                throw new WrongLoginCredentialsException();
//                System.out.println("email and password Failure");
            }
        } else if (userRepository.existsByUsername(userLoginRequest.getLogin())) {
            Optional<User> user = userRepository.findByUsername(userLoginRequest.getLogin());
            if (passwordEncoder.matches(userLoginRequest.getPassword(), user.get().getPassword())) {
//            if (userLoginRequest.getPassword().equals(user.get().getPassword())) {
                System.out.println("username and password Success");
                return user.get();
            } else {
                throw new WrongLoginCredentialsException();
//                System.out.println("username and password Failure");
            }
        }
        throw new WrongLoginCredentialsException();
    }

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyTakenException(user.getEmail());
        } else if (userRepository.findByUsername(user.getName()).isPresent()) {
            throw new UsernameAlreadyTakenException(user.getName());
        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());
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
        userRepository.deleteById(id);
    }

//    @Override
//    public Optional<User> doesEmailExist(String email) {
//        System.out.println("I went inside email");
//        return Optional.empty();
//    }
//
//    @Override
//    public Optional<User> doesUsernameExist(String username) {
//        System.out.println("I went inside username");
//        return Optional.empty();
//    }
}