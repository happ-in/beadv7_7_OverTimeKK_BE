package com.programmers.kdt.common.constant;

import java.util.Set;

public class OrderTypeCode {

    public static final String GENERAL = "GENERAL";
    public static final String STANDBY = "STANDBY";
    private static final Set<String> SUPPORTED_TYPES =
            Set.of(GENERAL, STANDBY);

    private OrderTypeCode() {
    }

    public static boolean isSupported(String orderType) {
        return SUPPORTED_TYPES.contains(orderType);
    }
}
