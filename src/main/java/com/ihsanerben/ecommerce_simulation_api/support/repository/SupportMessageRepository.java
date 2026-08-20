package com.ihsanerben.ecommerce_simulation_api.support.repository;

import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    Page<SupportMessage> findAllByConversationId(Long conversationId, Pageable pageable);
}
