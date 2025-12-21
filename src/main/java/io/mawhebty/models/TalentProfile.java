package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "talent_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class TalentProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ToString.Include
    private String fullName;

    @Column(name = "country", nullable = false)
    @ToString.Include
    private String country;

    @Column(name = "city", nullable = false)
    @ToString.Include
    private String city;

    @ToString.Include
    private Integer age;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    private Gender gender;

    private String shortBio;

    private String profilePicture;

    @ManyToOne
    @JoinColumn(name = "participation_type_id", nullable = false)
    private ParticipationType participationType;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private TalentCategory category;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    @JsonIgnore
    private TalentSubCategory subCategory;

    @OneToMany(mappedBy = "talentProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TalentCategoryFormValue> formValues = new ArrayList<>();
}
