package br.com.project.prontpet.services;

import br.com.project.prontpet.config.EvictPetCaches;
import br.com.project.prontpet.dtos.AppointmentCreateRequest;
import br.com.project.prontpet.dtos.AppointmentRequest;
import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.models.Pet;
import br.com.project.prontpet.repositories.AppointmentRepository;
import br.com.project.prontpet.repositories.ClinicRepository;
import br.com.project.prontpet.repositories.PetRepository;
import br.com.project.prontpet.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    private final CurrentUser currentUser;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PetRepository petRepository,
            ClinicRepository clinicRepository,
            CurrentUser currentUser
    ) {
        this.appointmentRepository = appointmentRepository;
        this.petRepository = petRepository;
        this.clinicRepository = clinicRepository;
        this.currentUser = currentUser;
    }

    /** Somente ADMIN (garantido por @PreAuthorize no controller). */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * ADMIN e o tutor dono veem todas as consultas do pet.
     * VET ve apenas as consultas do pet na propria clinica.
     */
    public List<Appointment> getAppointmentsByPet(Long petId) {
        Pet pet = findPetOrThrow(petId);

        boolean isOwner = isOwnedByCurrentUser(pet);
        if (!currentUser.isAdmin() && !currentUser.isVet() && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only see appointments for your own pets");
        }

        List<Appointment> appointments = appointmentRepository.findByPetId(petId);
        if (currentUser.isVet()) {
            return appointments.stream()
                    .filter(a -> a.getClinic() != null && currentUser.isFromClinic(a.getClinic().getId()))
                    .toList();
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByClinic(Long clinicId) {
        currentUser.requireClinicAccess(clinicId, "you can only see appointments for your own clinic");
        findClinicOrThrow(clinicId);
        return appointmentRepository.findByClinicId(clinicId);
    }

    public List<Appointment> getMyClinicAppointments() {
        Long clinicId = currentUser.clinicId();
        if (clinicId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only a clinic account has appointments");
        }
        return appointmentRepository.findByClinicId(clinicId);
    }

    public Appointment addAppointment(AppointmentCreateRequest request) {
        Clinic clinic = findClinicOrThrow(request.clinicId());
        Pet pet = findPetOrThrow(request.petId());

        if (!currentUser.isAdmin() && !isOwnedByCurrentUser(pet)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only schedule appointments for your own pets");
        }

        Appointment appointment = Appointment.builder()
                .clinic(clinic)
                .pet(pet)
                .appointmentDate(request.appointmentDate())
                .build();

        validateOpeningHours(appointment);
        if (appointmentRepository.existsByPetAndAppointmentDate(pet, appointment.getAppointmentDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Pet already has an appointment scheduled for this date and time");
        }

        return appointmentRepository.save(appointment);
    }

    /**
     * Preenchimento clinico pos-atendimento. So a clinica da consulta (ou ADMIN) pode fazer.
     * Pet, clinica e data da consulta nao mudam.
     */
    @Transactional
    @EvictPetCaches
    public Appointment updateAppointment(Long id, AppointmentRequest request) {
        Appointment appointment = findOrThrow(id);
        currentUser.requireClinicAccess(appointment.getClinic().getId(),
                "you can only update appointments of your own clinic");

        appointment.setSymptoms(request.symptoms());
        appointment.setDiagnosis(request.diagnosis());
        appointment.setObservations(request.observations());
        appointment.setUpdatedWeight(request.updatedWeight());

        Pet pet = appointment.getPet();
        pet.setWeight(request.updatedWeight());
        petRepository.save(pet);

        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        Appointment appointment = findOrThrow(id);

        boolean isOwnerOfPet = isOwnedByCurrentUser(appointment.getPet());
        boolean isVetOfClinic = appointment.getClinic() != null && currentUser.isFromClinic(appointment.getClinic().getId());

        if (!currentUser.isAdmin() && !isOwnerOfPet && !isVetOfClinic) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only cancel your own appointments");
        }

        if (appointment.getAppointmentDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot cancel a past appointment");
        }

        appointmentRepository.delete(appointment);
    }

    // --- helpers -----------------------------------------------------------------

    private void validateOpeningHours(Appointment appointment) {
        Clinic clinic = appointment.getClinic();
        LocalTime time = appointment.getAppointmentDate().toLocalTime();
        if (time.isBefore(clinic.getOpeningHours()) || time.isAfter(clinic.getClosingHours())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment time is outside clinic opening hours");
        }
    }

    private boolean isOwnedByCurrentUser(Pet pet) {
        return pet != null && pet.getOwner() != null && currentUser.isOwner(pet.getOwner().getId());
    }

    private Appointment findOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private Clinic findClinicOrThrow(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));
    }

    private Pet findPetOrThrow(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
    }
}
