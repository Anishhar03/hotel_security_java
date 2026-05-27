package com.example.hotel.model;

public record AuditEvent(
        String timestamp,
        String actor,
        String action,
        String target,
        String details
) {
}
