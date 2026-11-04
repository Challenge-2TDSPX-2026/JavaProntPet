package br.com.project.prontpet.dtos;

import br.com.project.prontpet.enums.Sex;
import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.models.Pet;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.time.LocalDateTime;


public record AppointmentResponse(
        Long id,
        String symptoms,
        String diagnosis,
        String observations,
        ClinicSummary clinic,
        PetSummary pet,
        LocalDateTime appointmentDate,
        Double updatedWeight
) {

    public record ClinicSummary(Long id, String name, String address, String phone) {
        static ClinicSummary from(Clinic c) {
            return c == null ? null : new ClinicSummary(c.getId(), c.getName(), c.getAddress(), c.getPhone());
        }
    }

    public record PetSummary(
            Long id,
            String name,
            String species,
            String breed,
            LocalDate birthDate,
            Double weight,
            Sex sex,
            Long ownerId
    ) {
        static PetSummary from(Pet p) {
            if (p == null) return null;
            Long ownerId = p.getOwner() != null ? p.getOwner().getId() : null;
            return new PetSummary(p.getId(), p.getName(), p.getSpecies(), p.getBreed(),
                    p.getBirthDate(), p.getWeight(), p.getSex(), ownerId);
        }
    }

    public static AppointmentResponse fromEntity(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getSymptoms(),
                a.getDiagnosis(),
                a.getObservations(),
                ClinicSummary.from(a.getClinic()),
                PetSummary.from(a.getPet()),
                a.getAppointmentDate(),
                a.getUpdatedWeight()
        );
    }
}