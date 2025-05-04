package org.pm.patientservice.service;

import org.pm.patientservice.dto.PagedPatientResponseDTO;
import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.exceptions.EmailAlreadyExistsException;
import org.pm.patientservice.exceptions.PatientNotFoundException;
import org.pm.patientservice.grpc.BillingServiceGrpcClient;
import org.pm.patientservice.kafka.KafkaProducer;
import org.pm.patientservice.mapper.PatientMapper;
import org.pm.patientservice.model.Patient;
import org.pm.patientservice.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }


    @Cacheable(
            value = "patients",
            key = "#page + '-' + #size + '-' + #sort + '-' + #sortField",
            condition = "#searchValue == ''"
    )
    public PagedPatientResponseDTO getAllPatients(int page, int size, String sort, String sortField, String searchValue) {

        log.info("[REDIS] : Cache miss - fetching from DB");

        try {
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        //request ->page =1
        //pageable -> page=0 -> zero based indexing
        Pageable pageable = PageRequest.of(page - 1, size,
                sort.equalsIgnoreCase("desc")
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending());

        Page<Patient> patientPage;

        if (searchValue == null || searchValue.isBlank()) {
            patientPage = patientRepository.findAll(pageable);
        } else {
            patientPage = patientRepository.findByNameContainingIgnoreCase(searchValue, pageable);
        }

        List<PatientResponseDTO> patientDtoResponseList = patientPage.getContent()
                .stream()
                .map(PatientMapper.INSTANCE::toDTO)
                .toList();

        return new PagedPatientResponseDTO(
                patientDtoResponseList,
                patientPage.getNumber() + 1,  //Zero based indexing
                patientPage.getSize(),
                patientPage.getTotalPages(),
                (int) patientPage.getTotalElements()
        );
    }


    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) throws ParseException {

        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exist" + patientRequestDTO.getEmail());
        }

        Patient createdPatient = patientRepository.save(PatientMapper.INSTANCE.toModel(patientRequestDTO));

        billingServiceGrpcClient.createNewBillingAccount(createdPatient.getId().toString(), createdPatient.getName(), createdPatient.getEmail());

        kafkaProducer.sendEvent(createdPatient);

        PatientResponseDTO patientResponseDTO = PatientMapper.INSTANCE.toDTO(createdPatient);

        return patientResponseDTO;
    }


    @Transactional
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) throws ParseException {

        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient with id : " + id + " doesn't exists"));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A patient with this email already exist" + patientRequestDTO.getEmail());
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
