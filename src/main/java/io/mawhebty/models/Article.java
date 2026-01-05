package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.mawhebty.enums.ArticleStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles",
        indexes = {
                @Index(name = "idx_article_status", columnList = "status"),
                @Index(name = "idx_article_category", columnList = "category_id"),
                @Index(name = "idx_article_sub_category", columnList = "sub_category_id"),
                @Index(name = "idx_article_published", columnList = "published_at"),
                @Index(name = "idx_article_created", columnList = "createdAt")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private TalentCategory category;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    @JsonIgnore
    private TalentSubCategory subCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ArticleStatusEnum status = ArticleStatusEnum.PUBLISHED;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "tags", length = 1000)
    private String tags; // Comma-separated tags

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SavedItem> savedByUsers = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("sectionOrder ASC")
    private List<ArticleSection> sections = new ArrayList<>();

    // Helper methods
    public void addSection(ArticleSection section) {
        sections.add(section);
        section.setArticle(this);
    }

    public void removeSection(ArticleSection section) {
        sections.remove(section);
        section.setArticle(null);
    }

    @PrePersist
    public void onCreate() {
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
        if (status == ArticleStatusEnum.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

}