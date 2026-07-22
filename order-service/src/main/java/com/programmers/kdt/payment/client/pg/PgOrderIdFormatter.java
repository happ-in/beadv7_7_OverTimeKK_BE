package com.programmers.kdt.payment.client.pg;

public final class PgOrderIdFormatter {
    public static String format(Long orderId) {
        return "ORDER-" + orderId;
    }
}
