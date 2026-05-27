package com.example.hotel.model;

import java.math.BigDecimal;
import java.util.Map;

public record RoomStats(
        long totalRooms,
        long availableRooms,
        long occupiedRooms,
        long maintenanceRooms,
        BigDecimal projectedNightlyRevenue,
        Map<RoomStatus, Long> roomsByStatus
) {
}
