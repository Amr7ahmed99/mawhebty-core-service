package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "researcher_profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResearcherProfile extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "user_type_id")
    private UserType userType;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private TalentCategory category;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    @JsonIgnore
    private TalentSubCategory subCategory;

    @Column(name = "country", nullable = false)
    @ToString.Include
    private String country;

    @Column(name = "city", nullable = false)
    @ToString.Include
    private String city;

    private String shortBio;
    private String profilePicture;
    private String contactPerson;// profile name (fullName)

    private String companyName;
    private String commercialRegNo;
    private String contactPhone;
}