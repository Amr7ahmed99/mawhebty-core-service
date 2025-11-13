package io.mawhebty.models;

import java.time.LocalDateTime;
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
import jakarta.persistence.Index;

@Entity
@Table(name = "media_moderations", indexes = {
//    @Index(name = "idx_moderation_post", columnList = "post_id"),
//    @Index(name = "idx_moderation_post", columnList = "talent_special_case_id"),
    @Index(name = "idx_moderation_status", columnList = "status_id"),
    @Index(name = "idx_moderation_checked", columnList = "checkedAt")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaModeration extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @OneToOne
//    @JoinColumn(name = "post_id", nullable = false, unique = true)
//    private Post post;

//    @OneToOne
//    @JoinColumn(name = "talent_special_case_id", nullable = false, unique = true)
//    private TalentSpecialCase talentSpecialCase;

    @Column(name = "reason")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private MediaModerationStatus status;

    @Column(name = "moderator_id")
    private Long moderatorId;// represents The Admin That Reviewed This Moderation, By Default is Null For AWS

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;
}
