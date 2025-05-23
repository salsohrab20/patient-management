package com.pm.stack.authservice.controller;

import com.pm.stack.authservice.dto.RegistrationRequestDTO;
import com.pm.stack.authservice.dto.RegistrationResponseDTO;
import com.pm.stack.authservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> register(@RequestBody RegistrationRequestDTO registrationRequestDTO) throws Exception {
        RegistrationResponseDTO registrationResponse = userService.performRegistration(registrationRequestDTO);
        return ResponseEntity.ok(registrationResponse);

    }
}
