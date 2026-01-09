package io.mawhebty.repository;

import io.mawhebty.enums.ArticleStatusEnum;
import io.mawhebty.models.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository
        extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    /* ===================== PUBLISHED ===================== */

    @Query("""
        SELECT a FROM Article a
        WHERE a.status = :published
        AND a.publishedAt <= :now
        ORDER BY a.publishedAt DESC
    """)
    Page<Article> findPublishedArticles(
            @Param("published") ArticleStatusEnum published,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );


    /* ===================== CATEGORY ===================== */

    @Query("""
        SELECT a FROM Article a
        WHERE a.status = :published
        AND a.publishedAt <= :now
        AND a.category.id = :categoryId
        ORDER BY a.publishedAt DESC
    """)
    Page<Article> findPublishedByCategory(
            @Param("categoryId") Long categoryId,
            @Param("published") ArticleStatusEnum published,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );


    /* ===================== SEARCH ===================== */

    @Query("""
        SELECT a FROM Article a
        WHERE a.status = :published
        AND (
            LOWER(a.titleEn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.titleAr) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.tags) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY a.publishedAt DESC
    """)
    Page<Article> searchArticles(
            @Param("query") String query,
            @Param("published") ArticleStatusEnum published,
            Pageable pageable
    );


    /* ===================== COUNTS ===================== */

    long countByStatus(ArticleStatusEnum status);


    /* ===================== IDS ===================== */

    List<Article> findByIdIn(List<Long> ids);


    /* ===================== SINGLE ===================== */

    Optional<Article> findByIdAndStatus(Long id, ArticleStatusEnum status);


    /* ===================== CATEGORY + SUB ===================== */

    @Query("""
        SELECT a FROM Article a
        WHERE a.status = :published
        AND a.category.id = :categoryId
        AND (:subCategoryId IS NULL OR a.subCategory.id = :subCategoryId)
        AND ( :search IS NULL OR LOWER(a.titleEn) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.titleAr) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.tags) LIKE LOWER(CONCAT('%', :search, '%')) )
        ORDER BY a.publishedAt DESC
    """)
    Page<Article> findByCategoryAndSubCategory(
            @Param("categoryId") Integer categoryId,
            @Param("subCategoryId") Integer subCategoryId,
            @Param("published") ArticleStatusEnum published,
            @Param("search") String search,
            Pageable pageable
    );
}
