package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.ClinicRequest;
import br.com.project.prontpet.dtos.ClinicResponse;
import br.com.project.prontpet.services.ClinicService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping
    @Operation(
            tags = "Clinic",
            summary = "Listar todas as clinicas",
            description = "Lista as clinicas para o tutor escolher onde agendar. Qualquer usuario autenticado."
    )
    public List<ClinicResponse> getClinics() {
        return clinicService.getClinics().stream().map(ClinicResponse::fromEntity).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            tags = "Clinic",
            summary = "Cadastrar nova clinica (ADMIN)",
            description = "Clinicas se cadastram por /auth/register (ROLE_VET). Este endpoint e restrito a ADMIN."
    )
    public ResponseEntity<ClinicResponse> addClinic(@Valid @RequestBody ClinicRequest clinicRequest) {
        var clinic = clinicService.addClinic(clinicRequest.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClinicResponse.fromEntity(clinic));
    }

    @PutMapping("/{id}")
    @Operation(
            tags = "Clinic",
            summary = "Atualizar clinica",
            description = "Atualiza os dados da clinica. Restrito a propria clinica (VET vinculado) ou ADMIN."
    )
    public ResponseEntity<ClinicResponse> updateClinic(@PathVariable Long id, @Valid @RequestBody ClinicRequest clinicRequest) {
        var clinic = clinicService.updateClinic(id, clinicRequest.toEntity());
        return ResponseEntity.ok(ClinicResponse.fromEntity(clinic));
    }

    @DeleteMapping("/{id}")
    @Operation(
            tags = "Clinic",
            summary = "Deletar clinica",
            description = "Remove a clinica e suas contas. Restrito a propria clinica ou ADMIN. 409 se houver consultas."
    )
    public ResponseEntity<Void> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.noContent().build();
    }
}
