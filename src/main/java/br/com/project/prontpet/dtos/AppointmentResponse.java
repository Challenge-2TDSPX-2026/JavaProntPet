package br.com.project.prontpet.dtos;

import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.models.Pet;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

public record AppointmentResponse(

        Long id,

        String symptoms,
        String dignosis,
        String observations,
        Clinic clinic,
        Pet pet,
        LocalDateTime appointmentDate,
        Double updatedWeight
) {
    public static AppointmentResponse fromEntity(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getSymptoms(),
                a.getDiagnosis(),
                a.getObservations(),
                a.getClinic(),
                a.getPet(),
                a.getAppointmentDate(),
                a.getUpdatedWeight()
        );
    }
}
