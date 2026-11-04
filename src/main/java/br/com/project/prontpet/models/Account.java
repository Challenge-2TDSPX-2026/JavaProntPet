package br.com.project.prontpet.models;

import br.com.project.prontpet.enums.Roles;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Data
@Entity
@Table(name = "TB_ACCOUNT")
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    // defesa em profundidade: o hash nunca vai para JSON nem para log
    @JsonIgnore
    @ToString.Exclude
    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @OneToOne
    private Owner owner;

    @ManyToOne
    private Clinic clinic;
}
