package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "talent_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class TalentCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Integer id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String nameEn; // ARTS, SPORTS, etc.

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String nameAr;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private Integer partnerId;

    @OneToMany(mappedBy = "talentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TalentCategoryFormKeys> formKeys = new ArrayList<>();

    @OneToMany(mappedBy = "talentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TalentSubCategory> talentSubCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TalentProfile> talentProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResearcherProfile> researcherProfiles = new ArrayList<>();
}