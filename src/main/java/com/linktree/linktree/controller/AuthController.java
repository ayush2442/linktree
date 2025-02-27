package com.linktree.linktree.controller;

import com.linktree.linktree.dto.AuthResponseDTO;
import com.linktree.linktree.dto.ForgotPasswordDTO;
import com.linktree.linktree.dto.LoginDTO;
import com.linktree.linktree.dto.RegisterDTO;
import com.linktree.linktree.service.AuthService;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Bucket authRateLimitBucket;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        if (!authRateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(authService.register(registerDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        if (!authRateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(authService.login(loginDTO));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        if (!authRateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        authService.forgotPassword(forgotPasswordDTO);
        return ResponseEntity.ok("Password reset email sent if account exists");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        if (!authRateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        boolean result = authService.resetPassword(token, newPassword);
        if (result) {
            return ResponseEntity.ok("Password has been reset successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to reset password");
        }
    }
}