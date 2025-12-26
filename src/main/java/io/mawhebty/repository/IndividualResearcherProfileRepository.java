package io.mawhebty.repository;

import io.mawhebty.models.IndividualResearcherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndividualResearcherProfileRepository extends IResearcherProfile, JpaRepository<IndividualResearcherProfile, Long> {
}