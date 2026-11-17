package com.salessavvy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewReturnRequest {

    @NotBlank(message = "Admin note is required")
    @Size(max = 500, message = "Admin note cannot exceed 500 characters")
    private String adminNote;

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
}