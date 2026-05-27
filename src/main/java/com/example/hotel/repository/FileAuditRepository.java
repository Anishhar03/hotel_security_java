package com.example.hotel.repository;

import com.example.hotel.model.AuditEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Repository
public class FileAuditRepository {

    private static final String FORMAT_VERSION = "v1";

    private final Path filePath;
    private final Lock lock = new ReentrantLock();

    public FileAuditRepository(@Value("${hotel.storage.audit-file:audit-log.txt}") String filePath) {
        this.filePath = Path.of(filePath);
    }

    public void append(AuditEvent event) {
        lock.lock();
        try {
            Files.createDirectories(filePath.toAbsolutePath().getParent());
            Files.writeString(filePath, serialize(event) + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write audit event", e);
        } finally {
            lock.unlock();
        }
    }

    public List<AuditEvent> findRecent(int limit) {
        lock.lock();
        try {
            if (!Files.exists(filePath)) {
                return Collections.emptyList();
            }
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<AuditEvent> events = new ArrayList<>();
            int start = Math.max(0, lines.size() - Math.max(1, limit));
            for (int i = lines.size() - 1; i >= start; i--) {
                parse(lines.get(i)).ifPresent(events::add);
            }
            return events;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read audit events", e);
        } finally {
            lock.unlock();
        }
    }

    private String serialize(AuditEvent event) {
        return String.join("|",
                FORMAT_VERSION,
                event.timestamp(),
                encode(event.actor()),
                encode(event.action()),
                encode(event.target()),
                encode(event.details()));
    }

    private java.util.Optional<AuditEvent> parse(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6 || !FORMAT_VERSION.equals(parts[0])) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new AuditEvent(
                parts[1],
                decode(parts[2]),
                decode(parts[3]),
                decode(parts[4]),
                decode(parts[5])));
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
}
