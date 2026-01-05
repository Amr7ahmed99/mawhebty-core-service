package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.mawhebty.exceptions.BadDataException;
import jakarta.persistence.*;
import lombok.*;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResearcherProfile extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

//    @ManyToOne
//    @JoinColumn(name = "user_type_id")
//    private UserType userType;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
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

    // Calculated fields
    @Transient
    private Integer followersCount;

    @Transient
    private Integer followingCount;

    public void updateFollowCounts(Integer followers, Integer following) {
        this.followersCount = followers;
        this.followingCount = following;
    }
}