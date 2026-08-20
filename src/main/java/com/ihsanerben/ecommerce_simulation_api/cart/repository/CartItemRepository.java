package com.ihsanerben.ecommerce_simulation_api.cart.repository;

import com.ihsanerben.ecommerce_simulation_api.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCart_User_Id(Long id, Long userId);
}
