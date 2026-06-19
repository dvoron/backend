package com.example.backend.service;

import com.example.backend.model.entity.User;

public interface AuthService {

    User login();

    User register();
}
