package br.com.project.prontpet.services;


import br.com.project.prontpet.dtos.AppointmentCreateRequest;
import br.com.project.prontpet.dtos.AppointmentRequest;
import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.models.Pet;
import br.com.project.prontpet.repositories.AppointmentRepository;
import br.com.project.prontpet.repositories.ClinicRepository;
import br.com.project.prontpet.repositories.PetRepository;
import br.com.project.prontpet.security.AccountUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    public AppointmentService(AppointmentRepository appointmentRepository,  PetRepository petRepository,  ClinicRepository clinicRepository) {
        this.appointmentRepository = appointmentRepository;
        this.petRepository = petRepository;
        this.clinicRepository = clinicRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByPet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();

        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isVet = account.getRole() == Roles.ROLE_VET;
        boolean isOwnerOfThisPet = account.getOwner() != null
                && pet.getOwner().getId().equals(account.getOwner().getId());

        if (!isAdmin && !isVet && !isOwnerOfThisPet) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only see appointments for your own pets");
        }

        return appointmentRepository.findByPetId(petId);
    }

    public List<Appointment> getAppointmentsByClinic(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();

        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isThisClinic = account.getClinic() != null
                && account.getClinic().getId().equals(clinicId);

        if (!isAdmin && !isThisClinic) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only see appointments for your own clinic");
        }

        return appointmentRepository.findByClinicId(clinicId);
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public void validateAppointment(Appointment appointment) {
        Clinic clinic = appointment.getClinic();
        LocalTime appointmentTime = appointment.getAppointmentDate().toLocalTime();
        if(appointmentTime.isBefore(clinic.getOpeningHours()) || appointmentTime.isAfter(clinic.getClosingHours())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Appointment time is outside clinic opening hours");
        }

    }
    public void validateToAddAppointment(Appointment appointment) {
        validateAppointment(appointment);
        if (appointmentRepository.existsByPetAndAppointmentDate(
                appointment.getPet(), appointment.getAppointmentDate()
        )){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Pet already has an appointment scheduled for this date and time");
        }
    }

    public Appointment addAppointment(AppointmentCreateRequest request) {
        Clinic clinic = clinicRepository.findById(request.clinicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();

        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isOwnerOfThisPet = account.getOwner() != null
                && pet.getOwner().getId().equals(account.getOwner().getId());

        if (!isAdmin && !isOwnerOfThisPet) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only schedule appointments for your own pets");
        }

        Appointment appointment = Appointment.builder()
                .clinic(clinic)
                .pet(pet)
                .appointmentDate(request.appointmentDate())
                .build();

        validateToAddAppointment(appointment);

        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Long id, AppointmentRequest request) {
        var optionalAppointment = getAppointmentById(id);
        if (optionalAppointment.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment Not Found");

        Clinic clinic = clinicRepository.findById(request.clinicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        Appointment updatedAppointment = Appointment.builder()
                .id(id)
                .speciality(request.speciality())
                .symptoms(request.symptoms())
                .diagnosis(request.diagnosis())
                .observations(request.observations())
                .clinic(clinic)
                .pet(pet)
                .appointmentDate(request.appointmentDate())
                .updatedWeight(request.updatedWeight())
                .build();

        validateAppointment(updatedAppointment);

        pet.setWeight(updatedAppointment.getUpdatedWeight());
        petRepository.save(pet);

        return appointmentRepository.save(updatedAppointment);
    }

    public void deleteAppointment(Long id) {
        var optionalAppointment = getAppointmentById(id);
        if (optionalAppointment.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment Not Found");
        appointmentRepository.deleteById(id);
    }
}