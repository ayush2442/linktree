package com.linktree.linktree.util;

import com.linktree.linktree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class ReferralCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int REFERRAL_CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generateCode();
            if (!userRepository.findByReferralCode(code).isPresent()) {
                return code;
            }
        }
        // If we couldn't generate a unique code after MAX_ATTEMPTS, append a timestamp
        return generateCode() + System.currentTimeMillis() % 10000;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(REFERRAL_CODE_LENGTH);
        for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}