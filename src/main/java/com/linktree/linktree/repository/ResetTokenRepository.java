package com.linktree.linktree.repository;

import com.linktree.linktree.model.ResetToken;
import com.linktree.linktree.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    Optional<ResetToken> findByToken(String token);
    Optional<ResetToken> findByUser(User user);
    void deleteByUser(User user);
}