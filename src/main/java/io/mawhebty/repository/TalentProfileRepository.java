package io.mawhebty.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.TalentProfile;

@Repository
public interface TalentProfileRepository extends JpaRepository<TalentProfile, Long> {

    Optional<TalentProfile> findByUserId(Long userId);
}