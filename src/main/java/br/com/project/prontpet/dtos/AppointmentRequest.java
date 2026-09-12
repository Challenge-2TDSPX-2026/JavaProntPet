package br.com.project.prontpet.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest (

        @NotBlank(message = "specialty is required")
        String speciality,

        @NotBlank(message = "symptoms is required")
        String symptoms,

        @NotBlank(message = "dignosis is required")
        String dignosis,

        @NotBlank(message = "observations is required")
        String observations,

        @NotNull(message = "clinicId is required")
        Long clinicId,

        @NotNull(message = "petId is required")
        Long petId,

        @NotNull(message = "appointmentDate is required")
        @FutureOrPresent(message = "appointmentDate might not be in the past")
        LocalDateTime appointmentDate,

        @NotNull(message =  "updatedWeight is required")
        Double updatedWeight
){

}