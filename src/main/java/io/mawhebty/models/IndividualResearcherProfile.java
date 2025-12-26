package io.mawhebty.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "individual_researcher_profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndividualResearcherProfile extends ResearcherProfile{
    @ToString.Include
    private String firstName;
    @ToString.Include
    private String lastName;
}