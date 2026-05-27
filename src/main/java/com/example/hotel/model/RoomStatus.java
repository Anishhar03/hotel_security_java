package com.example.hotel.model;

import java.util.Locale;

public enum RoomStatus {
    AVAILABLE,
    RESERVED,
    OCCUPIED,
    CLEANING,
    MAINTENANCE;

    public static RoomStatus from(String value) {
        if (value == null || value.isBlank()) {
            return AVAILABLE;
        }
        return RoomStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
