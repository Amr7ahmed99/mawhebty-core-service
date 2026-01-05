package io.mawhebty.repository;

import io.mawhebty.models.CompanyResearcherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyResearcherProfileRepository extends IResearcherProfileRepository, JpaRepository<CompanyResearcherProfile, Long> {
}