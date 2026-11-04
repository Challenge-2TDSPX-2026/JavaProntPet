package br.com.project.prontpet.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados clinicos preenchidos pela clinica apos o atendimento.
 * Pet, clinica e data NAO podem ser alterados aqui (antes o body aceitava
 * clinicId/petId e permitia mover a consulta para outra clinica).
 */
public record AppointmentRequest(

        @NotBlank(message = "symptoms is required")
        String symptoms,

        @NotBlank(message = "diagnosis is required")
        String diagnosis,

        @NotBlank(message = "observations is required")
        String observations,

        @NotNull(message = "updatedWeight is required")
        @DecimalMin(value = "0.5", message = "the weight must be at least 0.5")
        Double updatedWeight
) {
}
