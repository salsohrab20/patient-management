package com.pm.stack.authservice.service;

import com.pm.stack.authservice.dto.RegistrationRequestDTO;
import com.pm.stack.authservice.dto.RegistrationResponseDTO;
import com.pm.stack.authservice.model.User;
import com.pm.stack.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public RegistrationResponseDTO performRegistration(RegistrationRequestDTO registrationRequestDTO) throws Exception {
        if (userRepository.findByEmail(registrationRequestDTO.getEmail()).isPresent()) {
            throw new Exception("User Exists");
        }

        User user = new User();
        user.setEmail(registrationRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequestDTO.getPassword()));
        user.setRole(registrationRequestDTO.getRole());
        userRepository.save(user);

        return new RegistrationResponseDTO(user.getEmail());
    }
}
