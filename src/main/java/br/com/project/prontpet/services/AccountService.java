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
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final OwnerRepository ownerRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;

    public Account addAccount(AccountRequest request){
        if (accountRepository.findByEmail(request.email()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "e-mail already in use");

        Account account = request.toEntity();
        account.setPassword(passwordEncoder.encode(request.password()));

        if (request.role() == Roles.ROLE_ADMIN){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "you cannot add an administrator");
        }

        if (request.ownerId() != null){
            var owner = ownerRepository.findById(request.ownerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found"));
            account.setOwner(owner);
        }
        if (request.clinicId() != null){
            var clinic = clinicRepository.findById(request.clinicId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "clinic not found"));
            account.setClinic(clinic);
    }
        return accountRepository.save(account);
    }
}
