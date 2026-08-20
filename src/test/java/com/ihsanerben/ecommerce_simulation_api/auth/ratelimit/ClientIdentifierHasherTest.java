package com.ihsanerben.ecommerce_simulation_api.auth.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIdentifierHasherTest {

    @Test
    void forLog_returnsStableReferenceWithoutExposingClientId() {
        String clientId = "192.168.1.10";

        String reference = ClientIdentifierHasher.forLog(clientId);

        assertThat(reference)
                .hasSize(12)
                .isEqualTo(ClientIdentifierHasher.forLog(clientId))
                .doesNotContain(clientId);
    }
}
