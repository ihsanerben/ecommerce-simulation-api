package com.ihsanerben.ecommerce_simulation_api.catalog.repository;

import com.ihsanerben.ecommerce_simulation_api.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);
}
