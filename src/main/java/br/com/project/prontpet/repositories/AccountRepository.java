package br.com.project.prontpet.repositories;

import br.com.project.prontpet.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByOwnerId(Long ownerId);

    void deleteByClinicId(Long clinicId);
}
