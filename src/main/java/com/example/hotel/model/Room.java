package com.example.hotel.model;

import jakarta.validation.constraints.NotBlank;

public class Room {

    @NotBlank
    private String number;

    @NotBlank
    private String type;

    @NotBlank
    private String status;

    public Room() {
    }

    public Room(String number, String type, String status) {
        this.number = number;
        this.type = type;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

