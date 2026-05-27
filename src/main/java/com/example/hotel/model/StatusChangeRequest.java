package com.example.hotel.model;

import jakarta.validation.constraints.NotNull;

public class StatusChangeRequest {

    @NotNull
    private RoomStatus status;

    private String occupantName = "";

    private String notes = "";

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
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
}
