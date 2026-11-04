package br.com.project.prontpet.security;

import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Account;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentUser {

    public Account account() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountUserDetails details)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "authentication required");
        }
        return details.getAccount();
    }

    public boolean isAdmin() {
        return account().getRole() == Roles.ROLE_ADMIN;
    }

    public boolean isVet() {
        return account().getRole() == Roles.ROLE_VET;
    }

    public Long ownerId() {
        var owner = account().getOwner();
        return owner == null ? null : owner.getId();
    }

    public Long clinicId() {
        var clinic = account().getClinic();
        return clinic == null ? null : clinic.getId();
    }

    public boolean isOwner(Long ownerId) {
        return ownerId != null && ownerId.equals(ownerId());
    }

    public boolean isFromClinic(Long clinicId) {
        return clinicId != null && clinicId.equals(clinicId());
    }

    public void requireOwnerAccess(Long ownerId, String message) {
        if (!isAdmin() && !isOwner(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    public void requireClinicAccess(Long clinicId, String message) {
        if (!isAdmin() && !isFromClinic(clinicId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }
}
