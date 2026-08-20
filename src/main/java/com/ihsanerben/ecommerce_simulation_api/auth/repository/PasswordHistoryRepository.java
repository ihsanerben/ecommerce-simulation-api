package com.ihsanerben.ecommerce_simulation_api.auth.repository;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);
    List<PasswordHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
