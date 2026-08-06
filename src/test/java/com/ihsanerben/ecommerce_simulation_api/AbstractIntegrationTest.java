package com.ihsanerben.ecommerce_simulation_api;

import com.ihsanerben.ecommerce_simulation_api.config.TestcontainersConfiguration;
import com.ihsanerben.ecommerce_simulation_api.repository.RateLimitEntryRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    private RateLimitEntryRepository rateLimitEntryRepository;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void clearRateLimitEntries() {
        rateLimitEntryRepository.deleteAll();
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .filter(java.util.Objects::nonNull)
                .forEach(Cache::clear);
    }
}
