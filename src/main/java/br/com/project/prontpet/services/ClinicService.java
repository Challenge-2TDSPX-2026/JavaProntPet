package br.com.project.prontpet.services;

import br.com.project.prontpet.models.Clinic;
import br.com.project.prontpet.repositories.AccountRepository;
import br.com.project.prontpet.repositories.ClinicRepository;
import br.com.project.prontpet.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClinicService {

    private static final String OWN_CLINIC_ONLY = "you can only manage your own clinic";

    private final ClinicRepository clinicRepository;
    private final AccountRepository accountRepository;
    private final CurrentUser currentUser;

    public ClinicService(ClinicRepository clinicRepository, AccountRepository accountRepository, CurrentUser currentUser) {
        this.clinicRepository = clinicRepository;
        this.accountRepository = accountRepository;
        this.currentUser = currentUser;
    }

    public List<Clinic> getClinics() {
        return clinicRepository.findAll();
    }

    /** Somente ADMIN (garantido por @PreAuthorize no controller). */
    public Clinic addClinic(Clinic clinic) {
        if (clinicRepository.existsByCnpj(clinic.getCnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cnpj already registered");
        }
        return clinicRepository.save(clinic);
    }

    public Clinic updateClinic(Long id, Clinic newClinic) {
        currentUser.requireClinicAccess(id, OWN_CLINIC_ONLY);
        findOrThrow(id);
        newClinic.setId(id);
        return clinicRepository.save(newClinic);
    }

    @Transactional
    public void deleteClinic(Long id) {
        currentUser.requireClinicAccess(id, OWN_CLINIC_ONLY);
        findOrThrow(id);
        accountRepository.deleteByClinicId(id);
        clinicRepository.deleteById(id);
    }

    private Clinic findOrThrow(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "clinic not found"));
    }
}
