package com.example.hotel.repository;

import com.example.hotel.model.Room;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Very simple file-based "database" for rooms.
 * Data is stored as one JSON-like line per room: number|type|status
 */
@Repository
public class FileRoomRepository {

    private final Path filePath;
    private final Lock lock = new ReentrantLock();

    public FileRoomRepository() {
        this.filePath = Path.of("rooms-db.txt");
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
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    rooms.add(new Room(parts[0], parts[1], parts[2]));
                }
            }
            return rooms;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read rooms from file", e);
        } finally {
            lock.unlock();
        }
    }

    public Optional<Room> findByNumber(String number) {
        return findAll().stream()
                .filter(r -> r.getNumber().equals(number))
                .findFirst();
    }

    public Room save(Room room) {
        lock.lock();
        try {
            List<Room> existing = findAll();
            boolean updated = false;
            for (int i = 0; i < existing.size(); i++) {
                if (existing.get(i).getNumber().equals(room.getNumber())) {
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

    public void deleteByNumber(String number) {
        lock.lock();
        try {
            List<Room> existing = findAll();
            existing.removeIf(r -> r.getNumber().equals(number));
            writeAll(existing);
        } finally {
            lock.unlock();
        }
    }

    private void writeAll(List<Room> rooms) {
        List<String> lines = new ArrayList<>();
        for (Room room : rooms) {
            lines.add(String.join("|",
                    room.getNumber(),
                    room.getType(),
                    room.getStatus()));
        }
        try {
            Files.write(filePath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write rooms to file", e);
        }
    }
}

