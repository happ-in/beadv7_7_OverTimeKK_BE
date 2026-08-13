package com.programmers.kdt.standby.dto;

public record EmailNotificationRequest(
        String subject,
        String body
){
}
