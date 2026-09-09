package br.com.project.prontpet.services;

import br.com.project.prontpet.enums.Roles;
import br.com.project.prontpet.models.Owner;
import br.com.project.prontpet.models.Pet;
import br.com.project.prontpet.repositories.PetRepository;
import br.com.project.prontpet.security.AccountUserDetails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Cacheable(value = "pets")
    public Page<Pet> getPets(Pageable pageable){
        return petRepository.findAll(pageable);
    }

    public Optional<Pet> getPetById(Long id){
        return petRepository.findById(id);
    }

    //Filtro por Raça
    @Cacheable(value = "breeds")
    public Page<Pet> getByBreed(String breed, Pageable pageable){
        return petRepository.findByBreedContainingIgnoreCase(breed, pageable);
    }

    //Filtro por idade
    @Cacheable(value = "dates")
    public Page<Pet> getByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable){
        return  petRepository.findByBirthDateBetween(startDate, endDate, pageable);
    }

    //Filtro por Species
    @Cacheable(value = "species")
    public Page<Pet> getBySpecies(String species, Pageable pageable){
        return petRepository.findBySpeciesContainingIgnoreCase(species, pageable);
    }

    @Cacheable(value = "names")
    public Page<Pet> getByName(String name, Pageable pageable){
        return petRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @CacheEvict(value = {"pets", "breeds", "dates", "species", "names"}, allEntries = true)
    public Pet addPet(Pet pet){
        return petRepository.save(pet);
    }

    @CacheEvict(value = {"pets", "breeds", "dates", "species", "names"}, allEntries = true)
    public void deletePet (Long id) {
        var optionalPet = getPetById(id);
        if (optionalPet.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found");

        Pet existingPet = optionalPet.get();

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();



        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isOwnerOfThisPet = account.getOwner() != null
                && existingPet.getOwner().getId().equals(account.getOwner().getId());

        if (!isAdmin && !isOwnerOfThisPet) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only edit your own pets");
        }
        petRepository.deleteById(id);}

    @CacheEvict(value = {"pets", "breeds", "dates", "species", "names"}, allEntries = true)
    public Pet updatePet(Long id, Pet newPet){


        var optionalPet = getPetById(id);
        if (optionalPet.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found");

        Pet existingPet = optionalPet.get();

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();



        boolean isAdmin = account.getRole() == Roles.ROLE_ADMIN;
        boolean isOwnerOfThisPet = account.getOwner() != null
                && existingPet.getOwner().getId().equals(account.getOwner().getId());

        if (!isAdmin && !isOwnerOfThisPet) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only edit your own pets");
        }


        newPet.setId(id);
        petRepository.save(newPet);
        return newPet;
    }
}
