package br.com.project.prontpet.repositories;

import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByPetAndAppointmentDate(
            Pet pet,
            LocalDateTime appointmentDate
    );
    List<Appointment> findByPetId(Long petId);
}
