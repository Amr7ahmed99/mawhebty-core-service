package io.mawhebty.repository;

import io.mawhebty.models.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Get published articles with pagination
    @Query(value = "SELECT * FROM articles WHERE status = 'PUBLISHED' " +
            "AND published_at <= :now " +
            "ORDER BY published_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Article> findPublishedArticles(@Param("now") LocalDateTime now,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);

    // Get published articles by category
    @Query(value = "SELECT * FROM articles WHERE category_id = :category_id " +
            "AND status = 'PUBLISHED' " +
            "AND published_at <= :now " +
            "ORDER BY published_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Article> findPublishedByCategory(@Param("category") Long category_id,
                                          @Param("now") LocalDateTime now,
                                          @Param("limit") int limit,
                                          @Param("offset") int offset);

    // Search articles
    @Query(value = "SELECT * FROM articles WHERE " +
            "(LOWER(title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(meta_description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND status = 'PUBLISHED' " +
            "ORDER BY published_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Article> searchArticles(@Param("query") String query,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    // Count queries
    @Query(value = "SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED'",
            nativeQuery = true)
    Long countPublished();

    // Find articles by IDs
    @Query(value = "SELECT * FROM articles WHERE id IN :ids",
            nativeQuery = true)
    List<Article> findByIds(@Param("ids") List<Long> ids);

    // Find published article by ID
    @Query(value = "SELECT * FROM articles WHERE id = :id AND status = 'PUBLISHED'",
            nativeQuery = true)
    Optional<Article> findPublishedById(@Param("id") Long id);
}