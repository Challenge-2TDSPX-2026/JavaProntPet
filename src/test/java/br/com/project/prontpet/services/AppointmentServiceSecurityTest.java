package br.com.project.prontpet.services;

import br.com.project.prontpet.dtos.AppointmentRequest;
import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Account;
import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.models.Owner;
import br.com.project.prontpet.models.Pet;
import br.com.project.prontpet.repositories.AppointmentRepository;
import br.com.project.prontpet.repositories.ClinicRepository;
import br.com.project.prontpet.repositories.PetRepository;
import br.com.project.prontpet.security.AccountUserDetails;
import br.com.project.prontpet.security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regressao do feedback da Sprint 3: "VET edita consulta de outra clinica".
 * Os repositorios sao simulados com java.lang.reflect.Proxy (sem Mockito e sem banco).
 */
class AppointmentServiceSecurityTest {

    private Appointment appointment;
    private AppointmentService service;

    @SuppressWarnings("unchecked")
    private static <T> T fakeRepository(Class<T> type, Object entityToFind) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) ->
                switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(entityToFind);
                    case "save" -> args[0];
                    default -> null;
                });
    }

    @BeforeEach
    void setUp() {
        Clinic clinic1 = Clinic.builder().id(1L).name("Clinica 1").build();
        Pet pet = Pet.builder().id(5L).name("Rex").weight(10.0)
                .owner(Owner.builder().id(7L).name("Tutor").cpf("00000000000").build()).build();
        appointment = Appointment.builder().id(100L).clinic(clinic1).pet(pet)
                .appointmentDate(LocalDateTime.of(2030, 1, 10, 10, 0)).build();

        service = new AppointmentService(
                fakeRepository(AppointmentRepository.class, appointment),
                fakeRepository(PetRepository.class, pet),
                fakeRepository(ClinicRepository.class, clinic1),
                new CurrentUser());
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Roles role, Long clinicId) {
        Account account = Account.builder().email("u@x.com").role(role)
                .clinic(clinicId == null ? null : Clinic.builder().id(clinicId).build()).build();
        AccountUserDetails details = new AccountUserDetails(account);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    private AppointmentRequest clinicalData() {
        return new AppointmentRequest("coceira", "alergia", "retorno em 7 dias", 11.5);
    }

    @Test
    void vetFromAnotherClinicCannotUpdateAppointment() {
        loginAs(Roles.ROLE_VET, 2L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateAppointment(100L, clinicalData()));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals(null, appointment.getDiagnosis());
    }

    @Test
    void vetFromSameClinicUpdatesClinicalDataOnly() {
        loginAs(Roles.ROLE_VET, 1L);

        Appointment updated = service.updateAppointment(100L, clinicalData());

        assertEquals("alergia", updated.getDiagnosis());
        assertEquals(11.5, updated.getPet().getWeight());
        // pet, clinica e data nao mudam
        assertEquals(1L, updated.getClinic().getId());
        assertEquals(5L, updated.getPet().getId());
        assertEquals(LocalDateTime.of(2030, 1, 10, 10, 0), updated.getAppointmentDate());
    }

    @Test
    void adminCanUpdateAnyAppointment() {
        loginAs(Roles.ROLE_ADMIN, null);

        Appointment updated = service.updateAppointment(100L, clinicalData());

        assertEquals("alergia", updated.getDiagnosis());
    }
}
