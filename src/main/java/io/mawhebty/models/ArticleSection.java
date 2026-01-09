package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_sections",
        indexes = {
                @Index(name = "idx_section_article", columnList = "article_id"),
                @Index(name = "idx_section_order", columnList = "article_id, section_order")

        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "title_ar")
    private String titleAr;

    @Column(name = "content_en", columnDefinition = "TEXT")
    private String contentEn;

    @Column(name = "content_ar", columnDefinition = "TEXT")
    private String contentAr;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "embed_code")
    private String embedCode; // For embedded content like YouTube, Twitter, etc.

    // Helper method to determine if section has media
    public boolean hasMedia() {
        return imageUrl != null || videoUrl != null || embedCode != null;
    }
}