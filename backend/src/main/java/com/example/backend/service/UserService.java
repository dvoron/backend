package com.example.backend.service;



import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User signIn(LoginRequestDto userLoginRequest);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

//    Optional<User> doesEmailExist(String email);
//    Optional<User> doesUsernameExist(String username);
}
