package com.pm.stack.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegistrationRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Email(message = "Please provide a valid password")
    private String password;

    @NotBlank(message = "Role cannot be blank")
    @Email(message = "Please provide a valid role")
    private String role;

    public @NotBlank(message = "Email cannot be blank") @Email(message = "Please provide a valid email address") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email cannot be blank") @Email(message = "Please provide a valid email address") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Password cannot be blank") @Email(message = "Please provide a valid password") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password cannot be blank") @Email(message = "Please provide a valid password") String password) {
        this.password = password;
    }

    public @NotBlank(message = "Role cannot be blank") @Email(message = "Please provide a valid role") String getRole() {
        return role;
    }

    public void setRole(@NotBlank(message = "Role cannot be blank") @Email(message = "Please provide a valid role") String role) {
        this.role = role;
    }
}
