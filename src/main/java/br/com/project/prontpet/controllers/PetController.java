package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.PetRequest;
import br.com.project.prontpet.dtos.PetResponse;
import br.com.project.prontpet.models.Pet;
import br.com.project.prontpet.services.PetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // --- Listagens globais (ADMIN). Tutor usa /pets/me; VET ve pets via consultas da clinica. ---

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            tags = "Pet",
            summary = "Listar todos os pets (ADMIN)",
            description = "Retorna uma pagina com todos os pets. Filtros por 'species', 'breed', 'name' ou 'startDate'+'endDate'. Padrao: 10 por pagina."
    )
    public ResponseEntity<Page<PetResponse>> getPets(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        return page(petService.getPets(pageable));
    }

    @GetMapping(params = "species")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<PetResponse>> getPetsBySpecies(
            @PageableDefault(size = 10) Pageable pageable, @RequestParam String species) {
        return page(petService.getBySpecies(species, pageable));
    }

    @GetMapping(params = "breed")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<PetResponse>> getPetsByBreed(
            @PageableDefault(size = 10) Pageable pageable, @RequestParam String breed) {
        return page(petService.getByBreed(breed, pageable));
    }

    @GetMapping(params = {"startDate", "endDate"})
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<PetResponse>> getPetsByBirthDate(
            @PageableDefault(size = 10) Pageable pageable, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return page(petService.getByDateBetween(startDate, endDate, pageable));
    }

    @GetMapping(params = "name")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<PetResponse>> getPetsByName(
            @PageableDefault(size = 10) Pageable pageable, @RequestParam String name) {
        return page(petService.getByName(name, pageable));
    }

    // --- Tutor ---------------------------------------------------------------------

    @GetMapping("/me")
    @Operation(
            tags = "Pet",
            summary = "Listar meus pets",
            description = "Retorna uma pagina apenas com os pets do tutor autenticado."
    )
    public ResponseEntity<Page<PetResponse>> getMyPets(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        return page(petService.getMyPets(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            tags = "Pet",
            summary = "Buscar pet por ID",
            description = "Restrito ao tutor dono, ADMIN ou VET de clinica que tenha consulta com o pet. 404 se nao existir."
    )
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(PetResponse.fromEntity(petService.getPetById(id)));
    }

    @PostMapping
    @Operation(
            tags = "Pet",
            summary = "Cadastrar novo pet",
            description = "Cadastra um pet para o tutor autenticado e retorna 201."
    )
    public ResponseEntity<PetResponse> addPet(@Valid @RequestBody PetRequest petRequest) {
        Pet pet = petService.addPet(petRequest.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(PetResponse.fromEntity(pet));
    }

    @PutMapping("/{id}")
    @Operation(
            tags = "Pet",
            summary = "Atualizar pet",
            description = "Atualiza o pet. Restrito ao tutor dono ou ADMIN."
    )
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id, @Valid @RequestBody PetRequest petRequest) {
        Pet pet = petService.updatePet(id, petRequest.toEntity());
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @DeleteMapping("/{id}")
    @Operation(
            tags = "Pet",
            summary = "Deletar pet",
            description = "Remove o pet. Restrito ao tutor dono ou ADMIN. 409 se houver consulta agendada."
    )
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Page<PetResponse>> page(Page<Pet> pets) {
        return ResponseEntity.ok(pets.map(PetResponse::fromEntity));
    }
}
