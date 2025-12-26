package io.mawhebty.repository;

import io.mawhebty.models.CompanyResearcherProfile;
import io.mawhebty.models.ResearcherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyResearcherProfileRepository extends IResearcherProfile, JpaRepository<CompanyResearcherProfile, Long> {
}