package com.pm.stack.authservice.dto;

public class RegistrationResponseDTO {
    private String email ;

    public RegistrationResponseDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
