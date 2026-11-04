package br.com.project.prontpet.dtos;

import br.com.project.prontpet.models.Clinic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

import java.time.LocalTime;

public record ClinicRequest(

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "cnpj is required")
        @CNPJ(message = "the CNPJ must be valid")
        String cnpj,

        @NotBlank(message = "address is required")
        String address,

        @NotBlank(message = "phone is required")
        @Size(min = 11, max = 14, message = "the value must have between 11 and 14 characters")
        String phone,

        @NotNull(message = "openingHours is required")
        LocalTime openingHours,

        @NotNull(message = "closingHours is required")
        LocalTime closingHours
) {

    @JsonIgnore
    @AssertTrue(message = "openingHours must be before closingHours")
    public boolean isHoursValid() {
        return openingHours == null || closingHours == null || openingHours.isBefore(closingHours);
    }

    public Clinic toEntity() {
        return Clinic.builder()
                .name(name)
                .cnpj(cnpj)
                .address(address)
                .phone(phone)
                .openingHours(openingHours)
                .closingHours(closingHours)
                .build();
    }
}
