package io.mawhebty.projections;

public interface UserProfileProjection {
    Long getTalentProfileId();
    Long getIndividualResearcherProfileId();
    Long getCompanyResearcherProfileId();

    default boolean hasProfile() {
        return getTalentProfileId() != null || getIndividualResearcherProfileId() != null
                || getCompanyResearcherProfileId() != null;
    }
    
    default String getProfileType() {
        if (getTalentProfileId() != null) return "TALENT";
        if (getCompanyResearcherProfileId() != null) return "COMPANY_RESEARCHER";
        if (getIndividualResearcherProfileId() != null) return "INDIVIDUAL_RESEARCHER";
        return "NONE";
    }
}

