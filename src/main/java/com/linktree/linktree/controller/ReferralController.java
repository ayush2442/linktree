package com.linktree.linktree.controller;

import com.linktree.linktree.dto.ReferralStatsDTO;
import com.linktree.model.User;
import com.linktree.linktree.repository.UserRepository;
import com.linktree.linktree.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;
    private final UserRepository userRepository;

    @GetMapping("/referrals")
    public ResponseEntity<ReferralStatsDTO> getReferrals() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return ResponseEntity.ok(referralService.getReferralStats(currentUsername));
    }

    @GetMapping("/referral-stats")
    public ResponseEntity<ReferralStatsDTO> getReferralStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return ResponseEntity.ok(referralService.getReferralStats(currentUsername));
    }
}