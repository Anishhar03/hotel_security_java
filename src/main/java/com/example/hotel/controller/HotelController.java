package com.example.hotel.controller;

import com.example.hotel.model.AuditEvent;
import com.example.hotel.model.Room;
import com.example.hotel.model.RoomStats;
import com.example.hotel.model.StatusChangeRequest;
import com.example.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(path = "/public/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/rooms")
    public List<Room> listRooms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String sort) {
        return roomService.listRooms(status, type, floor, query, sort);
    }

    @GetMapping("/rooms/stats")
    public RoomStats stats() {
        return roomService.stats();
    }

    @GetMapping("/rooms/{number}")
    public ResponseEntity<Room> getRoom(@PathVariable String number) {
        Optional<Room> room = roomService.getByNumber(number);
        return room.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/rooms")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room request, Authentication authentication) {
        Room saved = roomService.createOrUpdate(request, actor(authentication));
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/admin/rooms/{number}/status")
    public ResponseEntity<Room> changeStatus(
            @PathVariable String number,
            @Valid @RequestBody StatusChangeRequest request,
            Authentication authentication) {
        return roomService.changeStatus(
                        number,
                        request.getStatus(),
                        request.getOccupantName(),
                        request.getNotes(),
                        actor(authentication))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/rooms/{number}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String number, Authentication authentication) {
        boolean removed = roomService.delete(number, actor(authentication));
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/audit")
    public List<AuditEvent> audit(@RequestParam(defaultValue = "25") int limit) {
        return roomService.recentAuditEvents(limit);
    }

    private String actor(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}
