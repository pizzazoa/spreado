package com.example.spreado.domain.auth.core.repository;

import com.example.spreado.domain.auth.core.entity.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRefreshRepository extends JpaRepository<UserRefreshToken, Long> {

    Optional<UserRefreshToken> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("DELETE FROM UserRefreshToken t WHERE t.user.id = :userId")
    void deleteByUserId(Long userId);
}
