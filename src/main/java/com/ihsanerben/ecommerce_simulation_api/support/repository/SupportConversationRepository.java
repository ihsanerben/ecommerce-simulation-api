package com.ihsanerben.ecommerce_simulation_api.support.repository;

import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, Long> {
    Page<SupportConversation> findAllByClientId(Long clientId, Pageable pageable);

    Optional<SupportConversation> findByIdAndAgentId(Long id, Long agentId);
}
