package br.com.project.prontpet.repositories;

import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByPetAndAppointmentDate(
            Pet pet,
            LocalDateTime appointmentDate
    );
    List<Appointment> findByPetId(Long petId);
    List<Appointment> findByClinicId(Long clinicId);


    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.pet.id = :petId AND a.appointmentDate >= :today")
    boolean hasFutureOrTodayAppointments(@Param("petId") Long petId, @Param("today") LocalDate today);
}
