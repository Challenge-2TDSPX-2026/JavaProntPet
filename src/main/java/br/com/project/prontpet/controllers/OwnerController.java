package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.OwnerRequest;
import br.com.project.prontpet.dtos.OwnerResponse;
import br.com.project.prontpet.services.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            tags = "Owner",
            summary = "Listar todos os donos (ADMIN)",
            description = "Retorna todos os tutores cadastrados. Restrito a ADMIN, pois inclui CPF."
    )
    public List<OwnerResponse> getOwners() {
        return ownerService.getOwners().stream().map(OwnerResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    @Operation(
            tags = "Owner",
            summary = "Buscar dono por ID",
            description = "Retorna os dados do tutor. Restrito ao proprio tutor ou ADMIN. 404 caso nao encontrado."
    )
    public ResponseEntity<OwnerResponse> getOwnerById(@PathVariable Long id) {
        return ResponseEntity.ok(OwnerResponse.fromEntity(ownerService.getOwnerById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            tags = "Owner",
            summary = "Cadastrar novo dono (ADMIN)",
            description = "Tutores comuns se cadastram por /auth/register. Este endpoint e restrito a ADMIN."
    )
    public ResponseEntity<OwnerResponse> addOwner(@Valid @RequestBody OwnerRequest ownerRequest) {
        var owner = ownerService.addOwner(ownerRequest.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(OwnerResponse.fromEntity(owner));
    }

    @PutMapping("/{id}")
    @Operation(
            tags = "Owner",
            summary = "Atualizar dono",
            description = "Atualiza os dados do tutor. Restrito ao proprio tutor ou ADMIN."
    )
    public ResponseEntity<OwnerResponse> updateOwner(@PathVariable Long id, @Valid @RequestBody OwnerRequest ownerRequest) {
        var owner = ownerService.updateOwner(id, ownerRequest.toEntity());
        return ResponseEntity.ok(OwnerResponse.fromEntity(owner));
    }

    @DeleteMapping("/{id}")
    @Operation(
            tags = "Owner",
            summary = "Deletar dono",
            description = "Remove o tutor e a conta dele. Restrito ao proprio tutor ou ADMIN. 409 se houver registros relacionados."
    )
    public ResponseEntity<Void> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
}
