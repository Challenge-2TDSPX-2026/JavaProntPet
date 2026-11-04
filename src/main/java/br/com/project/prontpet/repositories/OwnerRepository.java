package br.com.project.prontpet.repositories;

import br.com.project.prontpet.models.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmail(String email);

    boolean existsByCpf(String cpf);
}
