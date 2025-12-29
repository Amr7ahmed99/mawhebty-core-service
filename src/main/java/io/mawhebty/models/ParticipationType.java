package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participation_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 100)
    private String nameEn;  // PROJECT_IDEA / PERSONAL_TALENT / PATENT

    @Column(unique = true, nullable = false, length = 100)
    private String nameAr;

    @OneToMany(mappedBy = "participationType")
    @JsonIgnore
    @Builder.Default
    private List<TalentCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "participationType")
    @JsonIgnore
    @Builder.Default
    private List<TalentProfile> talentProfiles = new ArrayList<>();
}
