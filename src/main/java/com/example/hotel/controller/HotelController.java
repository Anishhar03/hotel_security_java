package com.example.hotel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@Validated
public class HotelController {

    // Public health/ping endpoint (not under /api)
    @GetMapping(path = "/public/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    // Authenticated endpoint: any authenticated user can view rooms
    @GetMapping("/api/rooms")
    public List<String> listRooms() {
        return List.of("101", "102", "201");
    }

    // Admin-only endpoint: create a room
    @PostMapping("/api/admin/rooms")
    public ResponseEntity<String> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok("Created room " + request.getNumber());
    }

    public static class RoomRequest {
        @NotBlank
        private String number;

        public RoomRequest() {}
        public RoomRequest(String number) { this.number = number; }

        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
    }
}