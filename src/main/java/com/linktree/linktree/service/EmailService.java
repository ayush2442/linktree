package com.linktree.linktree.service;

public interface EmailService {
    void sendWelcomeEmail(String to, String username);
    void sendPasswordResetEmail(String to, String token);
}