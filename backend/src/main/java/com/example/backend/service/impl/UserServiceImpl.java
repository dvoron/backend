package com.example.backend.service.impl;



import com.example.backend.exception.EmailAlreadyTakenException;
import com.example.backend.exception.UsernameAlreadyTakenException;
import com.example.backend.exception.WrongLoginCredentialsException;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @Override
    public User signIn(LoginRequestDto userLoginRequest) {
        if (userRepository.existsByEmail(userLoginRequest.getLogin())) {
            Optional<User> user = userRepository.findByEmail(userLoginRequest.getLogin());
            if (userLoginRequest.getPassword().equals(user.get().getPassword())) {
                System.out.println("email and password Success");
                return user.get();
            } else {
                throw new WrongLoginCredentialsException();
//                System.out.println("email and password Failure");
            }
        } else if (userRepository.existsByUsername(userLoginRequest.getLogin())) {
            Optional<User> user = userRepository.findByUsername(userLoginRequest.getLogin());
            if (userLoginRequest.getPassword().equals(user.get().getPassword())) {
                System.out.println("username and password Success");
                return user.get();
            } else {
                throw new WrongLoginCredentialsException();
//                System.out.println("username and password Failure");
            }
        }
        throw new WrongLoginCredentialsException();
    }

    @Override
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyTakenException(user.getEmail());
        } else if (userRepository.findByUsername(user.getName()).isPresent()) {
            throw new UsernameAlreadyTakenException(user.getName());
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

//        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());

        return userRepository.save(existingUser);
    }

    @Override
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