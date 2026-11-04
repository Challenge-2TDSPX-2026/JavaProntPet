package br.com.project.prontpet.services;

import br.com.project.prontpet.config.EvictPetCaches;
import br.com.project.prontpet.models.Pet;
import br.com.project.prontpet.repositories.AppointmentRepository;
import br.com.project.prontpet.repositories.PetRepository;
import br.com.project.prontpet.security.CurrentUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PetService {

    private static final String OWN_PETS_ONLY = "you can only manage your own pets";

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final CurrentUser currentUser;

    public PetService(AppointmentRepository appointmentRepository, PetRepository petRepository, CurrentUser currentUser) {
        this.appointmentRepository = appointmentRepository;
        this.petRepository = petRepository;
        this.currentUser = currentUser;
    }

    // --- Listagens globais: somente ADMIN (@PreAuthorize no controller) ---------

    @Cacheable(value = "pets")
    public Page<Pet> getPets(Pageable pageable) {
        return petRepository.findAll(pageable);
    }

    @Cacheable(value = "breeds")
    public Page<Pet> getByBreed(String breed, Pageable pageable) {
        return petRepository.findByBreedContainingIgnoreCase(breed, pageable);
    }

    @Cacheable(value = "dates")
    public Page<Pet> getByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return petRepository.findByBirthDateBetween(startDate, endDate, pageable);
    }

    @Cacheable(value = "species")
    public Page<Pet> getBySpecies(String species, Pageable pageable) {
        return petRepository.findBySpeciesContainingIgnoreCase(species, pageable);
    }

    @Cacheable(value = "names")
    public Page<Pet> getByName(String name, Pageable pageable) {
        return petRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    // --- Pets do tutor logado ----------------------------------------------------

    public Page<Pet> getMyPets(Pageable pageable) {
        Long ownerId = currentUser.ownerId();
        if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only a tutor account has pets");
        }
        return petRepository.findByOwnerId(ownerId, pageable);
    }

    /**
     * Quem pode ver um pet: ADMIN, o tutor dono, ou VET de uma clinica
     * que tenha consulta com esse pet.
     */
    public Pet getPetById(Long id) {
        Pet pet = findOrThrow(id);

        boolean allowed = currentUser.isAdmin()
                || isOwnedByCurrentUser(pet)
                || (currentUser.isVet() && currentUser.clinicId() != null
                    && appointmentRepository.existsByPetIdAndClinicId(pet.getId(), currentUser.clinicId()));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you cannot access this pet");
        }
        return pet;
    }

    @EvictPetCaches
    public Pet addPet(Pet pet) {
        var owner = currentUser.account().getOwner();
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only a tutor account can register a pet");
        }
        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    @EvictPetCaches
    public Pet updatePet(Long id, Pet newPet) {
        Pet existingPet = findOwnedPetOrThrow(id);
        newPet.setId(id);
        newPet.setOwner(existingPet.getOwner());
        return petRepository.save(newPet);
    }

    @EvictPetCaches
    public void deletePet(Long id) {
        Pet existingPet = findOwnedPetOrThrow(id);

        if (appointmentRepository.hasFutureOrTodayAppointments(existingPet.getId(), LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "has a scheduled appointment");
        }
        petRepository.deleteById(id);
    }

    // --- helpers -----------------------------------------------------------------

    private Pet findOrThrow(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
    }

    private boolean isOwnedByCurrentUser(Pet pet) {
        return pet.getOwner() != null && currentUser.isOwner(pet.getOwner().getId());
    }

    /** Busca o pet e exige que o usuario seja ADMIN ou o dono dele. */
    private Pet findOwnedPetOrThrow(Long id) {
        Pet pet = findOrThrow(id);
        if (!currentUser.isAdmin() && !isOwnedByCurrentUser(pet)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, OWN_PETS_ONLY);
        }
        return pet;
    }
}
