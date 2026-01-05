package io.mawhebty.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "individual_researcher_profile", indexes = {
        @Index(name = "idx_individual_researcher_category", columnList = "category_id"),
        @Index(name = "idx_individual_researcher_sub_category", columnList = "sub_category_id")
})
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