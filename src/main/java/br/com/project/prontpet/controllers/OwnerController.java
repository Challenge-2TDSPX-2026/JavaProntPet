package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.LoginRequest;
import br.com.project.prontpet.dtos.LoginResponse;
import br.com.project.prontpet.dtos.OwnerRequest;
import br.com.project.prontpet.dtos.OwnerResponse;
import br.com.project.prontpet.models.Owner;
import br.com.project.prontpet.services.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owners")
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping
    @Operation(
            tags = "Owner",
            summary = "Listar todos os donos",
            description = "Retorna uma lista com todos os donos de pets cadastrados no sistema."
    )
    public List<Owner> getOwner() {
        return ownerService.getOwners();
    }

    @GetMapping("/{id}")
    @Operation(
            tags = "Owner",
            summary = "Buscar dono por ID",
            description = "Retorna os dados de um dono específico pelo seu ID. Retorna 404 caso não encontrado."
    )
    public ResponseEntity<OwnerResponse> getOwnerById(@PathVariable Long id) {
        return ownerService.getOwnerById(id)
                .map((o) -> ResponseEntity.ok(OwnerResponse.fromEntity(o)))
                .orElse(ResponseEntity.notFound().build());
    }



    @PostMapping
    @Operation(
            tags = "Owner",
            summary = "Cadastrar novo dono",
            description = "Recebe os dados do dono via body, persiste no banco e retorna a entidade criada."
    )
    public ResponseEntity<OwnerResponse> addOwner(@Valid @RequestBody OwnerRequest ownerRequest) {
        Owner owner = ownerService.addOwner(ownerRequest.toEntity());
        return ResponseEntity.ok(OwnerResponse.fromEntity(owner));
    }

    @PutMapping("/{id}")
    @Operation(
            tags = "Owner",
            summary = "Atualizar dono",
            description = "Recebe o ID do dono e os novos dados via body, atualiza no banco e retorna a entidade atualizada."
    )
    public ResponseEntity<OwnerResponse> updateOwner(@PathVariable Long id, @Valid @RequestBody OwnerRequest ownerRequest) {
        Owner owner = ownerService.updateOwner(id, ownerRequest.toEntity());
        return ResponseEntity.ok(OwnerResponse.fromEntity(owner));
    }

    @DeleteMapping("/{id}")
    @Operation(
            tags = "Owner",
            summary = "Deletar dono",
            description = "Remove o dono com o ID informado do banco de dados. Retorna 204 sem conteúdo."
    )
    public ResponseEntity<Void> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
}