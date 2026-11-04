package br.com.project.prontpet.repositories;

import br.com.project.prontpet.models.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    boolean existsByCnpj(String cnpj);
}
