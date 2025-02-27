package com.linktree.linktree.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStatsDTO {
    private long totalReferrals;
    private long successfulReferrals;
    private String referralLink;
    private List<String> referredUsers;
}