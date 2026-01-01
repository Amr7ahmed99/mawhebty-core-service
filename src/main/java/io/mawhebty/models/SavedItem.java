package io.mawhebty.models;

import io.mawhebty.enums.SavedItemTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saved_items",
        indexes = {
                @Index(name = "idx_saved_item_user", columnList = "user_id"),
                @Index(name = "idx_saved_item_type", columnList = "item_type_id"),
                @Index(name = "idx_saved_item_created", columnList = "createdAt"),
                @Index(name = "idx_saved_item_unique", columnList = "user_id, item_type_id, item_id", unique = true)
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_type_id", nullable = false)
    private SavedItemType itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "tags")
    private String tags; // For organizing saved items

    // Relationships for easier querying
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;
}