package org.pm.patientservice.service;

import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.exceptions.EmailAlreadyExistsException;
import org.pm.patientservice.exceptions.PatientNotFoundException;
import org.pm.patientservice.grpc.BillingServiceGrpcClient;
import org.pm.patientservice.kafka.KafkaProducer;
import org.pm.patientservice.mapper.PatientMapper;
import org.pm.patientservice.model.Patient;
import org.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }


    public List<PatientResponseDTO> getAllPatients() {
        List<Patient> patientList = patientRepository.findAll();

        List<PatientResponseDTO> patientDtoResponseList = patientList.stream().map(
                PatientMapper.INSTANCE::toDTO
        ).collect(Collectors.toList());

        return patientDtoResponseList;
    }


    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) throws ParseException {

        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exist"
                    + patientRequestDTO.getEmail());
        }

        Patient createdPatient = patientRepository.save(
                PatientMapper.INSTANCE.toModel(patientRequestDTO)
        );

        billingServiceGrpcClient.createNewBillingAccount(createdPatient.getId().toString(), createdPatient.getName(), createdPatient.getEmail());

        kafkaProducer.sendEvent(createdPatient);

        PatientResponseDTO patientResponseDTO = PatientMapper.INSTANCE.toDTO(createdPatient);

        return patientResponseDTO;
    }


    @Transactional
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) throws ParseException {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient with id : " + id + " doesn't exists"));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A patient with this email already exist"
                    + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        patient.setDateOfBirth(formatter.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatePatient = patientRepository.save(patient);

        return PatientMapper.INSTANCE.toDTO(updatePatient);

    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }


}
