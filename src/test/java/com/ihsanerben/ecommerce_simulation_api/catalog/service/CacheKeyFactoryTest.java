package com.ihsanerben.ecommerce_simulation_api.catalog.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class CacheKeyFactoryTest {

    private final CacheKeyFactory cacheKeyFactory = new CacheKeyFactory();

    @Test
    void shouldCreateDifferentKeysForDifferentPagesAndSorting() {
        String firstPage = cacheKeyFactory.productSearch(
                3L, " Phone ", PageRequest.of(0, 20, Sort.by("price").ascending()));
        String secondPage = cacheKeyFactory.productSearch(
                3L, "phone", PageRequest.of(1, 20, Sort.by("price").ascending()));
        String descending = cacheKeyFactory.productSearch(
                3L, "phone", PageRequest.of(0, 20, Sort.by("price").descending()));

        assertThat(firstPage).isNotEqualTo(secondPage);
        assertThat(firstPage).isNotEqualTo(descending);
        assertThat(firstPage).startsWith("3|phone|0|20|");
    }
}
