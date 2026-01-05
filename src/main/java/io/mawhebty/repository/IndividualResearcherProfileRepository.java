package io.mawhebty.repository;

import io.mawhebty.models.IndividualResearcherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndividualResearcherProfileRepository extends IResearcherProfileRepository, JpaRepository<IndividualResearcherProfile, Long> {
}