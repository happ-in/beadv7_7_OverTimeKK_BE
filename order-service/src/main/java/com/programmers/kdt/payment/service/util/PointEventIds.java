package com.programmers.kdt.payment.service.util;

public final class PointEventIds {

    public static String useEventId(Long orderId) {
        return "ORDER:" + orderId + ":POINT_USE";
    }

    public static String rollbackEventId(Long orderId) {
        return "ORDER:" + orderId + ":POINT_ROLLBACK_REFUND";
    }

    public static String rollbackFailEventId(Long orderId) {
        return "ORDER:" + orderId + ":POINT_ROLLBACK_FAIL";
    }
}
