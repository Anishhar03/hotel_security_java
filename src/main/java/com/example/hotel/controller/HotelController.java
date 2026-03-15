package com.example.hotel.controller;

import com.example.hotel.model.Room;
import com.example.hotel.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api")
public class HotelController {

    private final RoomService roomService;

    public HotelController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Public health/ping endpoint
    @GetMapping(path = "/public/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    // Authenticated endpoint: any authenticated user can view rooms
    @GetMapping("/rooms")
    public List<Room> listRooms() {
        return roomService.listRooms();
    }

    // Admin-only endpoint: create or update a room
    @PostMapping("/admin/rooms")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room request) {
        Room saved = roomService.createOrUpdate(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/rooms/{number}")
    public ResponseEntity<Room> getRoom(@PathVariable String number) {
        Optional<Room> room = roomService.getByNumber(number);
        return room.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/rooms/{number}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String number) {
        roomService.delete(number);
        return ResponseEntity.noContent().build();
    }
}