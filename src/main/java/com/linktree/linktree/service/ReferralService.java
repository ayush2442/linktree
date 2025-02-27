package com.linktree.linktree.service;

import com.linktree.linktree.dto.ReferralStatsDTO;
import com.linktree.linktree.model.User;

public interface ReferralService {
    ReferralStatsDTO getReferralStats(User user);
    ReferralStatsDTO getReferralStats(String username);
}