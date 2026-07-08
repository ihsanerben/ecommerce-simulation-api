package com.ihsanerben.ecommerce_simulation_api;

import com.ihsanerben.ecommerce_simulation_api.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class EcommerceSimulationApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
