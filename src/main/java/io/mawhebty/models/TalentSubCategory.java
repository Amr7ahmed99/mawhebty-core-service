package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "talent_sub_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class TalentSubCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Integer id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String nameEn;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String nameAr;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private Integer partnerId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore // preventing serialization
    private TalentCategory talentCategory;

    @OneToMany(mappedBy = "talentSubCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TalentCategoryFormKeys> formKeys = new ArrayList<>();

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TalentProfile> talentProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResearcherProfile> researcherProfiles = new ArrayList<>();
}