package io.mawhebty.repository;

import io.mawhebty.models.ArticleSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleSectionRepository extends JpaRepository<ArticleSection, Long> {

    // Get sections by article with ordering
    @Query(value = "SELECT * FROM article_sections WHERE article_id = :articleId " +
            "ORDER BY section_order ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<ArticleSection> findByArticleIdWithPagination(@Param("articleId") Long articleId,
                                                       @Param("limit") int limit,
                                                       @Param("offset") int offset);

    // Get all sections by article
    @Query(value = "SELECT * FROM article_sections WHERE article_id = :articleId " +
            "ORDER BY section_order ASC",
            nativeQuery = true)
    List<ArticleSection> findByArticleId(@Param("articleId") Long articleId);

    // Get max section order
    @Query(value = "SELECT MAX(section_order) FROM article_sections WHERE article_id = :articleId",
            nativeQuery = true)
    Optional<Integer> findMaxSectionOrderByArticleId(@Param("articleId") Long articleId);

    // Count sections by article
    @Query(value = "SELECT COUNT(*) FROM article_sections WHERE article_id = :articleId",
            nativeQuery = true)
    Long countByArticleId(@Param("articleId") Long articleId);

    // Find section by article and order
    @Query(value = "SELECT * FROM article_sections WHERE article_id = :articleId " +
            "AND section_order = :sectionOrder",
            nativeQuery = true)
    Optional<ArticleSection> findByArticleIdAndSectionOrder(@Param("articleId") Long articleId,
                                                            @Param("sectionOrder") Integer sectionOrder);

    // Delete all sections by article
    @Query(value = "DELETE FROM article_sections WHERE article_id = :articleId",
            nativeQuery = true)
    void deleteByArticleId(@Param("articleId") Long articleId);
}