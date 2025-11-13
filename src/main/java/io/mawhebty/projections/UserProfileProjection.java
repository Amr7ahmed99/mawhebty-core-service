package io.mawhebty.projections;

public interface UserProfileProjection {
     public Long getTalentProfileId();
     public Long getResearcherProfileId();

    default boolean hasProfile() {
        return getTalentProfileId() != null || getResearcherProfileId() != null;
    }
    
    default String getProfileType() {
        if (getTalentProfileId() != null) return "TALENT";
        if (getResearcherProfileId() != null) return "RESEARCHER";
        return "NONE";
    }
}

