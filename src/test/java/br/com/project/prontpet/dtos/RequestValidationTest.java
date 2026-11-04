package br.com.project.prontpet.dtos;

import br.com.project.prontpet.enums.Roles;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Valida as regras dos DTOs de entrada (sem subir Spring nem banco). */
class RequestValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static <T> Set<String> messages(T target) {
        return VALIDATOR.validate(target).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    void loginRequiresEmailAndPassword() {
        Set<String> messages = messages(new LoginRequest("", ""));
        assertTrue(messages.contains("email is required"));
        assertTrue(messages.contains("password is required"));
    }

    @Test
    void registerRejectsInvalidEmailAndShortPassword() {
        AccountRequest request = new AccountRequest("not-an-email", "123", Roles.ROLE_USER, null, null);
        Set<String> messages = messages(request);
        assertTrue(messages.contains("password must have at least 6 characters"));
        assertTrue(messages.stream().anyMatch(m -> m.toLowerCase().contains("email")));
    }

    @Test
    void clinicRequiresOpeningHours() {
        ClinicRequest request = new ClinicRequest(
                "Clinica", "11222333000181", "Rua A, 1", "11999999999", null, LocalTime.of(18, 0));
        assertTrue(messages(request).contains("openingHours is required"));
    }

    @Test
    void clinicOpeningHoursMustBeBeforeClosingHours() {
        ClinicRequest request = new ClinicRequest(
                "Clinica", "11222333000181", "Rua A, 1", "11999999999", LocalTime.of(18, 0), LocalTime.of(8, 0));
        assertTrue(messages(request).contains("openingHours must be before closingHours"));
    }

    @Test
    void validClinicHasNoViolations() {
        ClinicRequest request = new ClinicRequest(
                "Clinica", "11222333000181", "Rua A, 1", "11999999999", LocalTime.of(8, 0), LocalTime.of(18, 0));
        assertTrue(messages(request).isEmpty());
    }
}
