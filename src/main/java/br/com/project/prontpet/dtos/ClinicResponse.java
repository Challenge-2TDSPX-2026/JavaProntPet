package br.com.project.prontpet.dtos;

import br.com.project.prontpet.models.Clinic;

import java.time.LocalTime;

public record ClinicResponse(
        Long id,
        String name,
        String cnpj,
        String address,
        String phone,
        LocalTime openingHours,
        LocalTime closingHours
) {

    public static ClinicResponse fromEntity(Clinic c) {
        return new ClinicResponse(
                c.getId(),
                c.getName(),
                c.getCnpj(),
                c.getAddress(),
                c.getPhone(),
                c.getOpeningHours(),
                c.getClosingHours()
        );
    }
}
