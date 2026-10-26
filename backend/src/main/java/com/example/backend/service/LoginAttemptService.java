package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPT = 5;
    private static final int LOCK_TIME_DURATION = 1; // in minutes

    private static class AttemptInfo {
        int count = 0;
        LocalDateTime lockTime;
    }

    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    public void loginFailed(String key) {
        attemptsCache.compute(key, (k, v) -> {
            if (v == null) {
                v = new AttemptInfo();
            }
            v.count++;
            if (v.count >= MAX_ATTEMPT && v.lockTime == null) {
                v.lockTime = LocalDateTime.now();
            }
            return v;
        });
    }

    public boolean isBlocked(String key) {
        AttemptInfo info = attemptsCache.get(key);
        if (info == null) {
            return false;
        }
        if (info.count >= MAX_ATTEMPT) {
            if (info.lockTime != null && info.lockTime.plusMinutes(LOCK_TIME_DURATION).isBefore(LocalDateTime.now())) {
                attemptsCache.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }
}
