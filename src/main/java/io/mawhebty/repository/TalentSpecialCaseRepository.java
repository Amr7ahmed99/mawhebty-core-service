package io.mawhebty.repository;

import io.mawhebty.models.TalentSpecialCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TalentSpecialCaseRepository extends JpaRepository<TalentSpecialCase, Long> {
    Optional<TalentSpecialCase> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
