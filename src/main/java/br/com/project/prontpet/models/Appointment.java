package br.com.project.prontpet.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TB_APPOINTMENT")
public class Appointment {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String speciality;
    private String symptoms;
    private String diagnosis;
    private String observations;
    private LocalDateTime appointmentDate;
    private Double updatedWeight;

    @ManyToOne
    private Clinic clinic;

    @ManyToOne
    private Pet pet;

}
