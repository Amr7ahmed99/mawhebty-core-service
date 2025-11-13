package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "phone"}),
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone"),
        @Index(name = "idx_user_status", columnList = "status_id"),
        @Index(name = "idx_user_role", columnList = "role_id")
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

    private String phone;
    private String password;

    @OneToMany(mappedBy = "ownerUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private UserRole role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private UserStatus status;

    @Column(name = "is_verified", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isVerified = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TalentProfile talentProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private ResearcherProfile researcherProfile;
}
