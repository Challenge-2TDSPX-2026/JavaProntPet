package br.com.project.prontpet.services;

import br.com.project.prontpet.dtos.AccountRequest;
import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Account;
import br.com.project.prontpet.repositories.AccountRepository;
import br.com.project.prontpet.repositories.ClinicRepository;
import br.com.project.prontpet.repositories.OwnerRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final OwnerRepository ownerRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account addAccount(AccountRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "e-mail already in use");
        }

        if (request.role() == Roles.ROLE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "you cannot add an administrator");
        }

        Account account = request.toEntity();
        account.setPassword(passwordEncoder.encode(request.password()));

        if (request.role() == Roles.ROLE_USER) {
            if (request.owner() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "owner data is required for this role");
            }
            if (ownerRepository.existsByCpf(request.owner().cpf())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "cpf already registered");
            }
            account.setOwner(ownerRepository.save(request.owner().toEntity()));
        }

        if (request.role() == Roles.ROLE_VET) {
            if (request.clinic() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clinic data is required for this role");
            }
            if (clinicRepository.existsByCnpj(request.clinic().cnpj())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "cnpj already registered");
            }
            account.setClinic(clinicRepository.save(request.clinic().toEntity()));
        }

        return accountRepository.save(account);
    }
}
