package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts",
    indexes = {
        @Index(name = "idx_post_owner", columnList = "owner_user_id"),
        @Index(name = "idx_post_status", columnList = "status_id"),
        @Index(name = "idx_post_visibility", columnList = "visibility_id"),
        @Index(name = "idx_post_created", columnList = "createdAt"),
        @Index(name = "idx_post_category", columnList = "category_id"),
        @Index(name = "idx_post_sub_category", columnList = "sub_category_id")
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

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private TalentCategory category;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    @JsonIgnore
    private TalentSubCategory subCategory;
}
