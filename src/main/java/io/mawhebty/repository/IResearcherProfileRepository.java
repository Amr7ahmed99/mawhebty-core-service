package io.mawhebty.repository;
import io.mawhebty.models.ResearcherProfile;
import java.util.Optional;

interface IResearcherProfileRepository {
        Optional<ResearcherProfile> findByUserId(Long userId);
}