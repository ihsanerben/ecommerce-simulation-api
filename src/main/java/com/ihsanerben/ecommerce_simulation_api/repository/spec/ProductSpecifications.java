package com.ihsanerben.ecommerce_simulation_api.repository.spec;

import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> categoryId == null
                ? null
                : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> nameContains(String search) {
        return (root, query, criteriaBuilder) -> (search == null || search.isBlank())
                ? null
                : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%");
    }
}
