package com.example.hotel.repository;

import com.example.hotel.model.Room;
import com.example.hotel.model.RoomStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Repository
public class FileRoomRepository {

    private static final String FORMAT_VERSION = "v2";

    private final Path filePath;
    private final Lock lock = new ReentrantLock();

    public FileRoomRepository(@Value("${hotel.storage.rooms-file:rooms-db.txt}") String filePath) {
        this.filePath = Path.of(filePath);
        initializeDefaultRooms();
    }

    public List<Room> findAll() {
        lock.lock();
        try {
            if (!Files.exists(filePath)) {
                return Collections.emptyList();
            }
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<Room> rooms = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                parseRoom(line).ifPresent(rooms::add);
            }
            return rooms;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read rooms from file", e);
        } finally {
            lock.unlock();
        }
    }

    public Optional<Room> findByNumber(String number) {
        return findAll().stream()
                .filter(r -> r.getNumber().equalsIgnoreCase(number))
                .findFirst();
    }

    public Room save(Room room) {
        lock.lock();
        try {
            List<Room> existing = findAll();
            boolean updated = false;
            for (int i = 0; i < existing.size(); i++) {
                if (existing.get(i).getNumber().equalsIgnoreCase(room.getNumber())) {
                    existing.set(i, room);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                existing.add(room);
            }
            writeAll(existing);
            return room;
        } finally {
            lock.unlock();
        }
    }

    public boolean deleteByNumber(String number) {
        lock.lock();
        try {
            List<Room> existing = findAll();
            boolean removed = existing.removeIf(r -> r.getNumber().equalsIgnoreCase(number));
            writeAll(existing);
            return removed;
        } finally {
            lock.unlock();
        }
    }

    private Optional<Room> parseRoom(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 9 && FORMAT_VERSION.equals(parts[0])) {
            return Optional.of(new Room(
                    decode(parts[1]),
                    decode(parts[2]),
                    RoomStatus.from(parts[3]),
                    parseInt(parts[4], 1),
                    parseBigDecimal(parts[5]),
                    decode(parts[6]),
                    decode(parts[7]),
                    parts[8]
            ));
        }

        if (parts.length >= 3) {
            return Optional.of(new Room(parts[0], parts[1], parts[2]));
        }

        return Optional.empty();
    }

    private void writeAll(List<Room> rooms) {
        List<String> lines = new ArrayList<>();
        for (Room room : rooms) {
            lines.add(String.join("|",
                    FORMAT_VERSION,
                    encode(room.getNumber()),
                    encode(room.getType()),
                    room.getStatus().name(),
                    String.valueOf(room.getFloor()),
                    room.getPricePerNight().toPlainString(),
                    encode(room.getOccupantName()),
                    encode(room.getNotes()),
                    room.getUpdatedAt()));
        }
        try {
            Files.createDirectories(filePath.toAbsolutePath().getParent());
            Files.write(filePath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write rooms to file", e);
        }
    }

    private void initializeDefaultRooms() {
        lock.lock();
        try {
            if (Files.exists(filePath)) {
                return;
            }
            writeAll(List.of(
                    new Room("101", "Deluxe", RoomStatus.AVAILABLE, 1, new BigDecimal("1800"), "", "Near lobby", ""),
                    new Room("102", "Suite", RoomStatus.OCCUPIED, 1, new BigDecimal("3200"), "Priya Mehta", "VIP guest", ""),
                    new Room("201", "Standard", RoomStatus.MAINTENANCE, 2, new BigDecimal("1200"), "", "AC inspection pending", ""),
                    new Room("301", "Executive", RoomStatus.RESERVED, 3, new BigDecimal("2600"), "Rahul Sen", "Late arrival", ""),
                    new Room("302", "Deluxe", RoomStatus.CLEANING, 3, new BigDecimal("1900"), "", "Housekeeping assigned", "")
            ));
        } finally {
            lock.unlock();
        }
    }

    private String encode(String value) {
        String safeValue = value == null ? "" : value;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
