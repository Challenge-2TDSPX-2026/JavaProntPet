package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.AppointmentCreateRequest; // NOVO import
import br.com.project.prontpet.dtos.AppointmentRequest;
import br.com.project.prontpet.dtos.AppointmentResponse;
import br.com.project.prontpet.dtos.ClinicResponse;
import br.com.project.prontpet.models.Appointment;
import br.com.project.prontpet.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }


    @GetMapping
    @Operation(
            tags = "Appointment",
            summary = "Listar todas as consultas",
            description = "Retorna uma lista com todas as consultas cadastradas no sistema."
    )
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PostMapping
    @Operation(
            tags = "Appointment",
            summary = "Agendar nova consulta", // MUDOU: texto do summary
            description = "Recebe petId, clinicId e appointmentDate via body, agenda a consulta e retorna a entidade criada com status 201." // MUDOU: texto da description
    )

    public ResponseEntity<AppointmentResponse> addAppointment(@Valid @RequestBody AppointmentCreateRequest appointmentRequest) {

        Appointment appointment = appointmentService.addAppointment(appointmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.fromEntity(appointment));
    }

    @PutMapping("/{id}")
    @Operation(
            tags = "Appointment",
            summary = "Atualizar consulta (pós-atendimento)", // MUDOU: texto do summary
            description = "Recebe o ID da consulta e os dados clínicos via body (preenchidos após o atendimento), atualiza no banco e retorna a entidade atualizada." // MUDOU: texto
    )
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentRequest appointmentRequest) {
        Appointment appointment = appointmentService.updateAppointment(id, appointmentRequest);
        return ResponseEntity.ok(AppointmentResponse.fromEntity(appointment));
    }


    @DeleteMapping("/{id}")
    @Operation(
            tags = "Appointment",
            summary = "Deletar consulta",
            description = "Remove a consulta com o ID informado do banco de dados. Retorna 204 sem conteúdo."
    )
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}