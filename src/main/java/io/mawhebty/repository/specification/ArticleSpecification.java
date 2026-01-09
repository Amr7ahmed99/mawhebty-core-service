package io.mawhebty.repository.specification;

import io.mawhebty.enums.ArticleStatusEnum;
import io.mawhebty.models.Article;
import org.springframework.data.jpa.domain.Specification;

public class ArticleSpecification {

    public static Specification<Article> hasCategory(Integer categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Article> hasSubCategory(Integer subCategoryId) {
        return (root, query, cb) -> subCategoryId == null ? null
                : cb.equal(root.get("subCategory").get("id"), subCategoryId);
    }

    public static Specification<Article> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank())
                return null;

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("titleEn")), like),
                    cb.like(cb.lower(root.get("titleAr")), like));
        };
    }

    public static Specification<Article> hasStatus(ArticleStatusEnum status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

}
