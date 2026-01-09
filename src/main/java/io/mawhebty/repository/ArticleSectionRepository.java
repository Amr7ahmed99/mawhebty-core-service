package io.mawhebty.repository;

import io.mawhebty.models.ArticleSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleSectionRepository extends JpaRepository<ArticleSection, Long> {

    List<ArticleSection> findByArticleIdOrderBySectionOrderAsc(Long articleId);

    Page<ArticleSection> findByArticleIdOrderBySectionOrderAsc(
            Long articleId, Pageable pageable);

    Optional<ArticleSection> findTopByArticleIdOrderBySectionOrderDesc(Long articleId);

    long countByArticleId(Long articleId);

    Optional<ArticleSection> findByArticleIdAndSectionOrder(
            Long articleId, Integer sectionOrder);

    void deleteByArticleId(Long articleId);
}
