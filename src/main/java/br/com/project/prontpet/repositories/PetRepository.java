package br.com.project.prontpet.repositories;

import br.com.project.prontpet.models.Owner;
import br.com.project.prontpet.models.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;


public interface PetRepository extends JpaRepository <Pet, Long> {

    Page<Pet> findBySpeciesContainingIgnoreCase(String species, Pageable pageable);
    Page<Pet> findByBreedContainingIgnoreCase(String race, Pageable pageable);
    Page<Pet> findByBirthDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Pet> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Pet> findByOwnerId(Long ownerId, Pageable pageable);

}
