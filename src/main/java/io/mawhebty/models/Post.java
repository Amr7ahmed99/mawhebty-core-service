package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts",
    indexes = {
        @Index(name = "idx_post_owner", columnList = "owner_user_id"),
        @Index(name = "idx_post_status", columnList = "status_id"),
        @Index(name = "idx_post_visibility", columnList = "visibility_id"),
        @Index(name = "idx_post_created", columnList = "createdAt")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private PostType type;

    private String title;
    private String caption;
    private String mediaUrl;
    private Integer durationSeconds;

    @ManyToOne
    @JoinColumn(name = "visibility_id", nullable = false)
    private PostVisibility visibility;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private PostStatus status;

    @OneToOne
    @JoinColumn(name = "media_moderation_id")
    private MediaModeration mediaModeration;
}
