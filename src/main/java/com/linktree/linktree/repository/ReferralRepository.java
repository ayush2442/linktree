package com.linktree.linktree.repository;

import com.linktree.linktree.model.Referral;
import com.linktree.linktree.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByReferrer(User referrer);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrer.id = :userId AND r.status = 'COMPLETED'")
    long countSuccessfulReferralsByUserId(Long userId);
}
