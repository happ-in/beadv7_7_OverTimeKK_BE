package com.programmers.kdt.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class TicketKeyGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TicketKeyGenerator() {}

    public static String generate() {
        byte[] bytes = new byte[16]; // 128bit 설정
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
