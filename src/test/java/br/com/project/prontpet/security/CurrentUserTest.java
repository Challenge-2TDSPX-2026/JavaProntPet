package br.com.project.prontpet.security;

import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Account;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.models.Owner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserTest {

    private final CurrentUser currentUser = new CurrentUser();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Account account) {
        AccountUserDetails details = new AccountUserDetails(account);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    private Account vetOfClinic(Long clinicId) {
        return Account.builder().email("vet@x.com").role(Roles.ROLE_VET)
                .clinic(Clinic.builder().id(clinicId).build()).build();
    }

    private Account tutorOfOwner(Long ownerId) {
        return Account.builder().email("tutor@x.com").role(Roles.ROLE_USER)
                .owner(Owner.builder().id(ownerId).build()).build();
    }

    @Test
    void vetCanAccessOwnClinicButNotAnotherOne() {
        loginAs(vetOfClinic(1L));

        assertDoesNotThrow(() -> currentUser.requireClinicAccess(1L, "no"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> currentUser.requireClinicAccess(2L, "no"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void tutorCanAccessOnlyOwnData() {
        loginAs(tutorOfOwner(10L));

        assertDoesNotThrow(() -> currentUser.requireOwnerAccess(10L, "no"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> currentUser.requireOwnerAccess(11L, "no"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void adminCanAccessAnything() {
        loginAs(Account.builder().email("admin@x.com").role(Roles.ROLE_ADMIN).build());

        assertDoesNotThrow(() -> currentUser.requireClinicAccess(99L, "no"));
        assertDoesNotThrow(() -> currentUser.requireOwnerAccess(99L, "no"));
    }

    @Test
    void withoutAuthenticationResponds401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, currentUser::account);
        assertEquals(401, ex.getStatusCode().value());
    }
}
