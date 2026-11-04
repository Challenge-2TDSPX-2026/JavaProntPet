package br.com.project.prontpet.dtos;

import br.com.project.prontpet.enums.Sex;
import br.com.project.prontpet.models.Pet;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record PetRequest(

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "species is required")
        String species,

        @NotBlank(message = "breed is required")
        String breed,

        @PastOrPresent(message = "the birth date might not be in the future")
        LocalDate birthDate,

        @NotNull(message = "weight is required")
        @DecimalMin(value = "0.5", message = "the weight must be at least 0.5")
        Double weight,

        @NotNull(message = "sex is required")
        Sex sex
) {

    public Pet toEntity() {
        return Pet.builder()
                .name(name)
                .species(species)
                .breed(breed)
                .birthDate(birthDate)
                .weight(weight)
                .sex(sex)
                .build();
    }
}
