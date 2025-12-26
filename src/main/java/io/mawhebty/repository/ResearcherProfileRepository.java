package io.mawhebty.repository;
import io.mawhebty.models.ResearcherProfile;
import java.util.Optional;

interface IResearcherProfile {
        Optional<ResearcherProfile> findByUserId(Long userId);
}