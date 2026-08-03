package com.ihsanerben.ecommerce_simulation_api.repository;

import com.ihsanerben.ecommerce_simulation_api.entity.RateLimitEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RateLimitEntryRepository extends JpaRepository<RateLimitEntry, Long> {

    Optional<RateLimitEntry> findByScopeAndClientId(String scope, String clientId);

    void deleteByScopeAndClientId(String scope, String clientId);

    @Query(value = "SELECT pg_advisory_xact_lock(hashtext(:scope), hashtext(:clientId))", nativeQuery = true)
    void lockBucket(@Param("scope") String scope, @Param("clientId") String clientId);

    @Modifying
    @Query("DELETE FROM RateLimitEntry entry WHERE entry.windowExpiresAt < :cutoff")
    void deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
