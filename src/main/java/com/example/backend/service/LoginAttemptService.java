package com.example.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPT = 5;
    private static final int LOCK_TIME_DURATION = 1; // in minutes
    private static final int ATTEMPT_WINDOW = 1; // minutes before attempt count resets

    private static class AttemptInfo {
        final int count;
        final LocalDateTime lastAttempt;
        final LocalDateTime lockTime;

        AttemptInfo(int count, LocalDateTime lastAttempt, LocalDateTime lockTime) {
            this.count = count;
            this.lastAttempt = lastAttempt;
            this.lockTime = lockTime;
        }
    }

    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    public void loginFailed(String key) {
        attemptsCache.compute(key, (k, v) -> {
            LocalDateTime now = LocalDateTime.now();
            
            // First failure
            if (v == null) {
                return new AttemptInfo(1, now, null);
            }
            
            // If already locked, just return the existing lock state
            if (v.lockTime != null && v.lockTime.plusMinutes(LOCK_TIME_DURATION).isAfter(now)) {
                return v;
            }

            // Calculate new count based on the time window
            int newCount = v.count;
            if (v.lastAttempt.plusMinutes(ATTEMPT_WINDOW).isBefore(now)) {
                newCount = 1; // Window expired, reset count
            } else {
                newCount++;
            }

            // Apply lock if threshold reached
            LocalDateTime newLockTime = (newCount >= MAX_ATTEMPT) ? now : null;
            return new AttemptInfo(newCount, now, newLockTime);
        });
    }

    public boolean isBlocked(String key) {
        AttemptInfo info = attemptsCache.get(key);
        
        if (info == null) {
            return false;
        }
        
        if (info.lockTime != null) {
            if (info.lockTime.plusMinutes(LOCK_TIME_DURATION).isBefore(LocalDateTime.now())) {
                attemptsCache.remove(key);
                return false;
            }
            return true;
        }
        
        return false;
    }

    // Runs every 60 seconds to clean up expired entries and prevent memory leaks
    @Scheduled(fixedRate = 60000)
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        attemptsCache.entrySet().removeIf(entry -> {
            AttemptInfo info = entry.getValue();
            if (info.lockTime != null) {
                return info.lockTime.plusMinutes(LOCK_TIME_DURATION).isBefore(now);
            } else {
                return info.lastAttempt.plusMinutes(ATTEMPT_WINDOW).isBefore(now);
            }
        });
    }
}
