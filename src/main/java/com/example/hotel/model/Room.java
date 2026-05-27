package com.example.hotel.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class Room {

    @NotBlank
    private String number;

    @NotBlank
    private String type;

    @NotNull
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Min(1)
    private int floor = 1;

    @DecimalMin("0.0")
    private BigDecimal pricePerNight = BigDecimal.ZERO;

    private String occupantName = "";

    private String notes = "";

    private String updatedAt = "";

    public Room() {
    }

    public Room(String number, String type, String status) {
        this.number = number;
        this.type = type;
        this.status = RoomStatus.from(status);
    }

    public Room(
            String number,
            String type,
            RoomStatus status,
            int floor,
            BigDecimal pricePerNight,
            String occupantName,
            String notes,
            String updatedAt) {
        this.number = number;
        this.type = type;
        this.status = status == null ? RoomStatus.AVAILABLE : status;
        this.floor = floor;
        this.pricePerNight = pricePerNight == null ? BigDecimal.ZERO : pricePerNight;
        this.occupantName = occupantName == null ? "" : occupantName;
        this.notes = notes == null ? "" : notes;
        this.updatedAt = updatedAt == null ? "" : updatedAt;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status == null ? RoomStatus.AVAILABLE : status;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight == null ? BigDecimal.ZERO : pricePerNight;
    }

    public String getOccupantName() {
        return occupantName;
    }

    public void setOccupantName(String occupantName) {
        this.occupantName = occupantName == null ? "" : occupantName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt == null ? "" : updatedAt;
    }
}
