package org.pm.patientservice.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import org.pm.patientservice.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/patients")
@Tag(name= "Patient" , description = "API for managing patients")
@OpenAPIDefinition
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> allPatients = patientService.getAllPatients();
        if(allPatients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(allPatients);
    }


    @PostMapping("/create")
    @Operation(summary = "Create Patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO) throws ParseException {
        if(isNull(patientRequestDTO)){
            return ResponseEntity.badRequest().build();
        }

        PatientResponseDTO createdPatient = patientService.createPatient(patientRequestDTO);

        return new ResponseEntity<>(createdPatient,HttpStatus.CREATED);

    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update Patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,
                                                            @Validated(Default.class) @RequestBody PatientRequestDTO patientRequestDTO) throws ParseException {
        if(isNull(patientRequestDTO)){
            return ResponseEntity.badRequest().build();
        }

        PatientResponseDTO createdPatient = patientService.updatePatient(id,patientRequestDTO);

        return ResponseEntity.ok().body(createdPatient);

    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete Patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }


}
