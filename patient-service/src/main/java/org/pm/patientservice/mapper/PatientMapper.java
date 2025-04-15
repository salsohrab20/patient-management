package org.pm.patientservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.model.Patient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Mapper
public interface PatientMapper {
    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);

    @Mapping(target="dateOfBirth", expression = "java(mapDateToString(patient.getDateOfBirth()))")
    PatientResponseDTO toDTO(Patient patient);

    @Mapping(target="dateOfBirth", expression = "java(mapStringToDate(patientRequestDTO.getDateOfBirth()))")
    @Mapping(target="registeredDate", expression = "java(mapStringToDate(patientRequestDTO.getRegisteredDate()))")
    Patient toModel(PatientRequestDTO patientRequestDTO) throws ParseException;

    default Date mapStringToDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(date);

    }

    default String mapDateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }
}
