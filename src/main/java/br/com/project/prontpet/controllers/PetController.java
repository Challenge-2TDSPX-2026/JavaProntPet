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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    @Operation(
            tags = "Pet",
            summary = "Listar todos os pets",
            description = "Retorna uma página com todos os pets cadastrados. É possível filtrar por: 'species', 'breed', 'name' ou intervalo de datas com 'startDate' e 'endDate'. Tamanho padrão de 10 por página."
    )
    public ResponseEntity<Page<PetResponse>> getPets(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<PetResponse> pets = petService.getPets(pageable).map(PetResponse::fromEntity);
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/me")
    @Operation(
            tags = "Pet",
            summary = "Listar meus pets",
            description = "Retorna uma página apenas com os pets do tutor autenticado."
    )
    public ResponseEntity<Page<PetResponse>> getMyPets(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<PetResponse> pets = petService.getMyPets(pageable).map(PetResponse::fromEntity);
        return ResponseEntity.ok(pets);
    }

    @GetMapping(params = "species")
    public ResponseEntity<Page<PetResponse>> getPetsBySpecies(Pageable pageable, @RequestParam String species) {
        Page<PetResponse> pets = petService.getBySpecies(species, pageable).map(PetResponse::fromEntity);
        return ResponseEntity.ok(pets);
    }

    @GetMapping(params = "breed")
    public ResponseEntity<Page<PetResponse>> getPetsByBreeds(Pageable pageable, @RequestParam String breed) {
        Page<PetResponse> pets = petService.getByBreed(breed, pageable).map(PetResponse::fromEntity);
        return ResponseEntity.ok(pets);
    }

    @GetMapping(params = {"startDate", "endDate"})
    public ResponseEntity<Page<PetResponse>> getPetsByOwners(Pageable pageable, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        Page<PetResponse> pets = petService.getByDateBetween(startDate, endDate, pageable).map(PetResponse::fromEntity);
        return ResponseEntity.ok(pets);
    }

    @GetMapping(params = "name")
    public ResponseEntity<Page<PetResponse>> getPetsByName(Pageable pageable, @RequestParam String name) {
        Page<PetResponse> pets = petService.getByName(name, pageable).map(PetResponse::fromEntity);
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    @Operation(
            tags = "Pet",
            summary = "Buscar pet por ID",
            description = "Retorna os dados de um pet específico pelo seu ID. Retorna 404 caso não encontrado."
    )
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return petService.getPetById(id)
                .map((p) -> ResponseEntity.ok(PetResponse.fromEntity(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            tags = "Pet",
            summary = "Cadastrar novo pet",
            description = "Recebe os dados de um pet via body, persiste no banco e retorna a entidade criada com status 201."
    )
    public ResponseEntity<PetResponse> addPet(@Valid @RequestBody PetRequest petRequest) {
        Pet pet = petService.addPet(petRequest.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(PetResponse.fromEntity(pet));
    }

    @PutMapping("/{id}")
    @Operation(
            tags = "Pet",
            summary = "Atualizar pet",
            description = "Recebe o ID do pet e os novos dados via body, atualiza no banco e retorna a entidade atualizada."
    )
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id, @Valid @RequestBody PetRequest petRequest) {
        Pet pet = petService.updatePet(id, petRequest.toEntity());
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @DeleteMapping("/{id}")
    @Operation(
            tags = "Pet",
            summary = "Deletar pet",
            description = "Remove o pet com o ID informado do banco de dados. Retorna 204 sem conteúdo."
    )
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }
}