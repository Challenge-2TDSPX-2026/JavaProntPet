package br.com.project.prontpet.dtos;

import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Account;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.models.Owner;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountRequest(

        @NotBlank(message = "email is required")
        @Email
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 6, message = "password must have at least 6 characters")
        String password,

        @NotNull(message = "role is required")
        Roles role,

        @Valid
        OwnerRequest owner,
        @Valid
        ClinicRequest clinic

) {
    public Account toEntity() {
        return Account.builder()
                .email(email)
                .password(password)
                .role(role)
                .build();
    }
}