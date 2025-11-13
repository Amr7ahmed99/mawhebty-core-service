package io.mawhebty.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.ResearcherProfile;

@Repository
public interface ResearcherProfileRepository extends JpaRepository<ResearcherProfile, Long> {
        Optional<ResearcherProfile> findByUserId(Long userId);
}