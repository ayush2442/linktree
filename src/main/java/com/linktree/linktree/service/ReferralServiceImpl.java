package com.linktree.linktree.service;

import com.linktree.linktree.dto.ReferralStatsDTO;
import com.linktree.linktree.exception.ResourceNotFoundException;
import com.linktree.linktree.model.User;
import com.linktree.linktree.repository.ReferralRepository;
import com.linktree.linktree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private final UserRepository userRepository;
    private final ReferralRepository referralRepository;

    @Value("${app.referral.base-url}")
    private String referralBaseUrl;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "referralStats", key = "#user.id")
    public ReferralStatsDTO getReferralStats(User user) {
        long totalReferrals = userRepository.countReferralsByUserId(user.getId());
        long successfulReferrals = referralRepository.countSuccessfulReferralsByUserId(user.getId());

        List<String> referredUsers = user.getReferrals().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        return ReferralStatsDTO.builder()
                .totalReferrals(totalReferrals)
                .successfulReferrals(successfulReferrals)
                .referralLink(referralBaseUrl + user.getReferralCode())
                .referredUsers(referredUsers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralStatsDTO getReferralStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return getReferralStats(user);
    }
}
