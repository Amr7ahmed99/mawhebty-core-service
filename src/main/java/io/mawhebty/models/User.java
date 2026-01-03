package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "country_code", "phone_number"}),
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone_number"),
        @Index(name = "idx_user_country_code", columnList = "country_code"),
        @Index(name = "idx_user_status", columnList = "status_id"),
        @Index(name = "idx_user_role", columnList = "role_id"),
        @Index(name = "idx_user_type", columnList = "user_type_id")
    }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "country_code")
    private String countryCode;
    private String password;

    @OneToMany(mappedBy = "ownerUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<SavedItem> savedItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "user_type_id")
    private UserType userType;// company/individual
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private UserStatus status;

    @Column(name = "is_verified", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isVerified = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TalentProfile talentProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CompanyResearcherProfile companyResearcherProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private IndividualResearcherProfile individualResearcherProfile;
}
