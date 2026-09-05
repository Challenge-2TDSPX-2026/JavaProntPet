package br.com.project.prontpet.dtos;

import br.com.project.prontpet.models.Account;

public record AccountResponse(
        Long id,
        String email,
        String role
) {
    public static AccountResponse fromEntity(Account a) {
        return new AccountResponse(a.getId(), a.getEmail(), a.getRole().name());
    }
}