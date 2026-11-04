package br.com.project.prontpet.services;

import br.com.project.prontpet.models.Owner;
import br.com.project.prontpet.repositories.AccountRepository;
import br.com.project.prontpet.repositories.OwnerRepository;
import br.com.project.prontpet.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OwnerService {

    private static final String OWN_ACCOUNT_ONLY = "you can only access your own account";

    private final OwnerRepository ownerRepository;
    private final AccountRepository accountRepository;
    private final CurrentUser currentUser;

    public OwnerService(OwnerRepository ownerRepository, AccountRepository accountRepository, CurrentUser currentUser) {
        this.ownerRepository = ownerRepository;
        this.accountRepository = accountRepository;
        this.currentUser = currentUser;
    }

    /** Somente ADMIN (garantido por @PreAuthorize no controller). */
    public List<Owner> getOwners() {
        return ownerRepository.findAll();
    }

    /** Somente ADMIN (garantido por @PreAuthorize no controller). */
    public Owner addOwner(Owner owner) {
        if (ownerRepository.existsByCpf(owner.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cpf already registered");
        }
        return ownerRepository.save(owner);
    }

    public Owner getOwnerById(Long id) {
        currentUser.requireOwnerAccess(id, OWN_ACCOUNT_ONLY);
        return findOrThrow(id);
    }

    public Owner updateOwner(Long id, Owner newOwner) {
        currentUser.requireOwnerAccess(id, OWN_ACCOUNT_ONLY);
        findOrThrow(id);
        newOwner.setId(id);
        return ownerRepository.save(newOwner);
    }

    @Transactional
    public void deleteOwner(Long id) {
        currentUser.requireOwnerAccess(id, OWN_ACCOUNT_ONLY);
        findOrThrow(id);
        accountRepository.deleteByOwnerId(id);
        ownerRepository.deleteById(id);
    }

    private Owner findOrThrow(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found"));
    }
}
