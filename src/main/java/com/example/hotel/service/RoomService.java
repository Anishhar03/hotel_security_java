package com.example.hotel.service;

import com.example.hotel.model.Room;
import com.example.hotel.repository.FileRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final FileRoomRepository repository;

    public RoomService(FileRoomRepository repository) {
        this.repository = repository;
    }

    public List<Room> listRooms() {
        return repository.findAll();
    }

    public Room createOrUpdate(Room room) {
        return repository.save(room);
    }

    public Optional<Room> getByNumber(String number) {
        return repository.findByNumber(number);
    }

    public void delete(String number) {
        repository.deleteByNumber(number);
    }
}

