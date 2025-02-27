package com.linktree.linktree.service;

import com.linktree.linktree.exception.DuplicateResourceException;
import com.linktree.linktree.exception.InvalidOperationException;
import com.linktree.linktree.exception.ResourceNotFoundException;
import com.linktree.linktree.model.ResetToken;
import com.linktree.linktree.model.User;
import com.linktree.linktree.repository.ResetTokenRepository;
import com.linktree.linktree.repository.UserRepository;
import com.linktree.linktree.security.JwtTokenProvider;
import com.linktree.linktree.util.ReferralCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final ReferralCodeGenerator referralCodeGenerator;

    @Value("${app.reset-token.expiration}")
    private long resetTokenExpiration;

    @Value("${app.referral.base-url}")
    private String referralBaseUrl;

    @Override
    @Transactional
    public com.linktree.linktree.dto.AuthResponseDTO register(com.linktree.linktree.dto.RegisterDTO registerDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User referrer = null;
        if (registerDTO.getReferralCode() != null && !registerDTO.getReferralCode().isEmpty()) {
            referrer = userRepository.findByReferralCode(registerDTO.getReferralCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid referral code"));
        }

        // Generate unique referral code for new user
        String referralCode = referralCodeGenerator.generateUniqueCode();

        // Create new user
        User user = User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .referralCode(referralCode)
                .referredBy(referrer)
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = tokenProvider.generateToken(savedUser.getUsername());

        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());

        return com.linktree.linktree.dto.AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .referralCode(savedUser.getReferralCode())
                .referralLink(referralBaseUrl + savedUser.getReferralCode())
                .build();
    }

    @Override
    public com.linktree.linktree.dto.AuthResponseDTO login(com.linktree.linktree.dto.LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsernameOrEmail(),
                        loginDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = authentication.getName();
        String token = tokenProvider.generateToken(username);

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")));

        return com.linktree.linktree.dto.AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .referralCode(user.getReferralCode())
                .referralLink(referralBaseUrl + user.getReferralCode())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(com.linktree.linktree.dto.ForgotPasswordDTO forgotPasswordDTO) {
        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + forgotPasswordDTO.getEmail()));

        // Delete any existing token
        resetTokenRepository.findByUser(user).ifPresent(resetTokenRepository::delete);

        // Create new token
        String token = UUID.randomUUID().toString();
        ResetToken resetToken = ResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMillis(resetTokenExpiration))
                .build();

        resetTokenRepository.save(resetToken);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (resetToken.isExpired()) {
            resetTokenRepository.delete(resetToken);
            throw new InvalidOperationException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken);
        return true;
    }
}
