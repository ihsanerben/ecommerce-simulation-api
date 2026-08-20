package com.ihsanerben.ecommerce_simulation_api.auth.ratelimit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ClientIdentifierHasher {

    private static final int LOG_HASH_LENGTH = 12;

    private ClientIdentifierHasher() {
    }

    public static String forLog(String clientId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(clientId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, LOG_HASH_LENGTH);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available.", exception);
        }
    }
}
