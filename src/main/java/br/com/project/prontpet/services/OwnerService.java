package br.com.project.prontpet.services;

import br.com.project.prontpet.dtos.LoginRequest;
import br.com.project.prontpet.dtos.LoginResponse;
import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Owner;
import br.com.project.prontpet.repositories.OwnerRepository;
import br.com.project.prontpet.security.AccountUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public List<Owner> getOwners(){return ownerRepository.findAll();}

    public Owner addOwner(Owner owner){
        return ownerRepository.save(owner);
    }

    public Optional<Owner> getOwnerById(Long id){
        return ownerRepository.findById(id);
    }

    public Optional<Owner> getOwnerByEmail(String email){
        return ownerRepository.findByEmail(email);
    }

    public void deleteOwner(Long id){

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();

        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isSameOwner = account.getOwner() != null && account.getOwner().getId().equals(id);

        if (!isAdmin && !isSameOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only edit your own account");
        }var optionalOwner = getOwnerById(id);
        if (optionalOwner.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }
        ownerRepository.deleteById(id);
    }

    public Owner updateOwner(Long id, Owner newOwner){

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();

        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isSameOwner = account.getOwner() != null && account.getOwner().getId().equals(id);

        if (!isAdmin && !isSameOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only edit your own account");
        }

        var optionalOwner = getOwnerById(id);
        if (optionalOwner.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found");
        newOwner.setId(id);
        ownerRepository.save(newOwner);
        return newOwner;
    }

}

