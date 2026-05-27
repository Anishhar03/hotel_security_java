package com.example.hotel.service;

import com.example.hotel.model.AuditEvent;
import com.example.hotel.model.Room;
import com.example.hotel.model.RoomStats;
import com.example.hotel.model.RoomStatus;
import com.example.hotel.repository.FileAuditRepository;
import com.example.hotel.repository.FileRoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoomService {

    private final FileRoomRepository roomRepository;
    private final FileAuditRepository auditRepository;

    public RoomService(FileRoomRepository roomRepository, FileAuditRepository auditRepository) {
        this.roomRepository = roomRepository;
        this.auditRepository = auditRepository;
    }

    public List<Room> listRooms(String status, String type, Integer floor, String query, String sort) {
        Stream<Room> stream = roomRepository.findAll().stream();

        if (status != null && !status.isBlank()) {
            RoomStatus requestedStatus = RoomStatus.from(status);
            stream = stream.filter(room -> room.getStatus() == requestedStatus);
        }

        if (type != null && !type.isBlank()) {
            String requestedType = type.toLowerCase(Locale.ROOT);
            stream = stream.filter(room -> room.getType().toLowerCase(Locale.ROOT).contains(requestedType));
        }

        if (floor != null) {
            stream = stream.filter(room -> room.getFloor() == floor);
        }

        if (query != null && !query.isBlank()) {
            String requestedQuery = query.toLowerCase(Locale.ROOT);
            stream = stream.filter(room ->
                    room.getNumber().toLowerCase(Locale.ROOT).contains(requestedQuery)
                            || room.getType().toLowerCase(Locale.ROOT).contains(requestedQuery)
                            || room.getOccupantName().toLowerCase(Locale.ROOT).contains(requestedQuery)
                            || room.getNotes().toLowerCase(Locale.ROOT).contains(requestedQuery));
        }

        return stream.sorted(comparatorFor(sort)).collect(Collectors.toList());
    }

    public RoomStats stats() {
        List<Room> rooms = roomRepository.findAll();
        Map<RoomStatus, Long> roomsByStatus = new EnumMap<>(RoomStatus.class);
        for (RoomStatus status : RoomStatus.values()) {
            roomsByStatus.put(status, 0L);
        }
        rooms.forEach(room -> roomsByStatus.merge(room.getStatus(), 1L, Long::sum));

        BigDecimal projectedRevenue = rooms.stream()
                .filter(room -> room.getStatus() == RoomStatus.OCCUPIED || room.getStatus() == RoomStatus.RESERVED)
                .map(Room::getPricePerNight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RoomStats(
                rooms.size(),
                roomsByStatus.get(RoomStatus.AVAILABLE),
                roomsByStatus.get(RoomStatus.OCCUPIED),
                roomsByStatus.get(RoomStatus.MAINTENANCE),
                projectedRevenue,
                roomsByStatus);
    }

    public Room createOrUpdate(Room room, String actor) {
        normalize(room);
        boolean exists = roomRepository.findByNumber(room.getNumber()).isPresent();
        Room saved = roomRepository.save(room);
        audit(actor, exists ? "ROOM_UPDATED" : "ROOM_CREATED", room.getNumber(),
                "%s room saved as %s".formatted(room.getType(), room.getStatus()));
        return saved;
    }

    public Optional<Room> getByNumber(String number) {
        return roomRepository.findByNumber(number);
    }

    public Optional<Room> changeStatus(String number, RoomStatus status, String occupantName, String notes, String actor) {
        Optional<Room> existing = roomRepository.findByNumber(number);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        Room room = existing.get();
        RoomStatus previous = room.getStatus();
        room.setStatus(status);
        room.setOccupantName(occupantName);
        room.setNotes(notes);
        normalize(room);
        Room saved = roomRepository.save(room);
        audit(actor, "STATUS_CHANGED", number, "%s -> %s".formatted(previous, status));
        return Optional.of(saved);
    }

    public boolean delete(String number, String actor) {
        boolean removed = roomRepository.deleteByNumber(number);
        if (removed) {
            audit(actor, "ROOM_DELETED", number, "Room removed from inventory");
        }
        return removed;
    }

    public List<AuditEvent> recentAuditEvents(int limit) {
        return auditRepository.findRecent(limit);
    }

    private void normalize(Room room) {
        room.setNumber(room.getNumber().trim());
        room.setType(room.getType().trim());
        room.setOccupantName(room.getOccupantName().trim());
        room.setNotes(room.getNotes().trim());
        room.setUpdatedAt(Instant.now().toString());
        if (room.getFloor() < 1) {
            room.setFloor(1);
        }
        if (room.getPricePerNight() == null || room.getPricePerNight().compareTo(BigDecimal.ZERO) < 0) {
            room.setPricePerNight(BigDecimal.ZERO);
        }
    }

    private void audit(String actor, String action, String target, String details) {
        auditRepository.append(new AuditEvent(
                Instant.now().toString(),
                actor == null || actor.isBlank() ? "system" : actor,
                action,
                target,
                details));
    }

    private Comparator<Room> comparatorFor(String sort) {
        String requestedSort = sort == null ? "number" : sort.toLowerCase(Locale.ROOT);
        return switch (requestedSort) {
            case "type" -> Comparator.comparing(Room::getType, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Room::getNumber, String.CASE_INSENSITIVE_ORDER);
            case "status" -> Comparator.comparing(Room::getStatus)
                    .thenComparing(Room::getNumber, String.CASE_INSENSITIVE_ORDER);
            case "floor" -> Comparator.comparingInt(Room::getFloor)
                    .thenComparing(Room::getNumber, String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparing(Room::getPricePerNight)
                    .thenComparing(Room::getNumber, String.CASE_INSENSITIVE_ORDER);
            case "updated" -> Comparator.comparing(Room::getUpdatedAt, Comparator.nullsLast(String::compareTo)).reversed();
            default -> Comparator.comparing(Room::getNumber, String.CASE_INSENSITIVE_ORDER);
        };
    }
}
