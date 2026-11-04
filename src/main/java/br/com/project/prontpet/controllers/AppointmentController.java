package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.AppointmentCreateRequest;
import br.com.project.prontpet.dtos.AppointmentRequest;
import br.com.project.prontpet.dtos.AppointmentResponse;
import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            tags = "Appointment",
            summary = "Listar todas as consultas (ADMIN)",
            description = "Retorna todas as consultas do sistema. Restrito a ADMIN."
    )
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return list(appointmentService.getAllAppointments());
    }

    @GetMapping(params = "petId")
    @Operation(
            tags = "Appointment",
            summary = "Listar consultas de um pet",
            description = "Restrito ao dono do pet, ADMIN ou VET (que ve apenas as consultas da propria clinica)."
    )
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPet(@RequestParam Long petId) {
        return list(appointmentService.getAppointmentsByPet(petId));
    }

    @GetMapping("/clinic/me")
    @Operation(
            tags = "Appointment",
            summary = "Listar consultas da minha clinica",
            description = "Retorna todas as consultas da clinica autenticada."
    )
    public ResponseEntity<List<AppointmentResponse>> getMyClinicAppointments() {
        return list(appointmentService.getMyClinicAppointments());
    }

    @GetMapping("/clinic/{clinicId}")
    @Operation(
            tags = "Appointment",
            summary = "Listar consultas de uma clinica",
            description = "Restrito a propria clinica (VET vinculado) ou ADMIN."
    )
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByClinic(@PathVariable Long clinicId) {
        return list(appointmentService.getAppointmentsByClinic(clinicId));
    }

    @PostMapping
    @Operation(
            tags = "Appointment",
            summary = "Agendar nova consulta",
            description = "Recebe petId, clinicId e appointmentDate. Restrito ao dono do pet ou ADMIN. Valida horario da clinica e conflito de horario do pet."
    )
    public ResponseEntity<AppointmentResponse> addAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        Appointment appointment = appointmentService.addAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.fromEntity(appointment));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_VET', 'ROLE_ADMIN')")
    @Operation(
            tags = "Appointment",
            summary = "Registrar atendimento (pos-consulta)",
            description = "Dados clinicos (sintomas, diagnostico, observacoes, peso). Restrito a clinica da consulta ou ADMIN. Pet, clinica e data nao mudam."
    )
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(AppointmentResponse.fromEntity(appointmentService.updateAppointment(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            tags = "Appointment",
            summary = "Cancelar consulta",
            description = "Restrito ao dono do pet, a clinica da consulta ou ADMIN. Nao cancela consulta passada."
    )
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<List<AppointmentResponse>> list(List<Appointment> appointments) {
        return ResponseEntity.ok(appointments.stream().map(AppointmentResponse::fromEntity).toList());
    }
}
