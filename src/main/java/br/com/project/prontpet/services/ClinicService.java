package br.com.project.prontpet.services;

import br.com.project.prontpet.dtos.ClinicRequest;
import br.com.project.prontpet.dtos.ClinicResponse;
import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.repositories.ClinicRepository;
import br.com.project.prontpet.security.AccountUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    public List<Clinic> getClinics() {
        return clinicRepository.findAll();
    }

    public Optional<Clinic> getClinicById(Long id) {
        return clinicRepository.findById(id);
    }

    public Clinic addClinic(Clinic clinic) {
        return clinicRepository.save(clinic);
    }

    public Clinic updateClinic(Long id, Clinic newClinic) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();

        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isSameClinic = account.getClinic() != null && account.getClinic().getId().equals(id);

        if (!isAdmin && !isSameClinic) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only edit your own clinic");
        }
        var optionalClinic = getClinicById(id);
        if(optionalClinic.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found");
        newClinic.setId(id);
        clinicRepository.save(newClinic);
        return newClinic;
    }

    public void deleteClinic(Long id) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();

        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isSameClinic = account.getClinic() != null && account.getClinic().getId().equals(id);

        if (!isAdmin && !isSameClinic) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only edit your own clinic");
        }

        var optionalClinic = getClinicById(id);
        if(optionalClinic.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found");
        clinicRepository.deleteById(id);
    }
}
