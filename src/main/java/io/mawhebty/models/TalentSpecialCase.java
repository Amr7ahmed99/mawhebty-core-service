package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "talent_special_case")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentSpecialCase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "s3_file_url", nullable = false)
    private String s3FileUrl;

    @Column(name = "short_brief", length = 2000)
    private String shortBrief;

    @OneToOne
    @JoinColumn(name = "media_moderation_id")
    private MediaModeration mediaModeration;
}
