package br.com.project.prontpet.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public record AppointmentCreateRequest(

        @NotNull(message = "petId is required")
        Long petId,

        @NotNull(message = "clinicId is required")
        Long clinicId,

        @NotNull(message = "appointmentDate is required")
        @FutureOrPresent(message = "appointmentDate might not be in the past")
        LocalDateTime appointmentDate

) {
}