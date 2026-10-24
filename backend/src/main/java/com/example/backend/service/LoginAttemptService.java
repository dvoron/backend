package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPT_USERNAME = 5;
    private static final int MAX_ATTEMPT_IP = 10;
    private static final int LOCK_TIME_DURATION = 1; // in minutes

    private static class AttemptInfo {
        int count = 0;
        LocalDateTime lockTime;
    }

    private final ConcurrentHashMap<String, AttemptInfo> attemptsByUsername = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptInfo> attemptsByIp = new ConcurrentHashMap<>();

    public void loginSucceeded(String username, String ip) {
        attemptsByUsername.remove(username);
        attemptsByIp.remove(ip);
    }

    public void loginFailed(String username, String ip) {
        recordFailure(attemptsByUsername, username, MAX_ATTEMPT_USERNAME);
        recordFailure(attemptsByIp, ip, MAX_ATTEMPT_IP);
    }

    private void recordFailure(ConcurrentHashMap<String, AttemptInfo> cache, String key, int maxAttempt) {
        cache.compute(key, (k, v) -> {
            if (v == null) {
                v = new AttemptInfo();
            }
            v.count++;
            if (v.count >= maxAttempt && v.lockTime == null) {
                v.lockTime = LocalDateTime.now();
            }
            return v;
        });
    }

    public boolean isUsernameBlocked(String username) {
        return isBlocked(attemptsByUsername, username, MAX_ATTEMPT_USERNAME);
    }

    public boolean isIpBlocked(String ip) {
        return isBlocked(attemptsByIp, ip, MAX_ATTEMPT_IP);
    }

    private boolean isBlocked(ConcurrentHashMap<String, AttemptInfo> cache, String key, int maxAttempt) {
        AttemptInfo info = cache.get(key);
        if (info == null) {
            return false;
        }
        if (info.count >= maxAttempt) {
            if (info.lockTime != null && info.lockTime.plusMinutes(LOCK_TIME_DURATION).isBefore(LocalDateTime.now())) {
                cache.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }
}
