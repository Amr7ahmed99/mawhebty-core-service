package io.mawhebty.repository.specification;

import io.mawhebty.models.Post;
import io.mawhebty.models.PostStatus;
import io.mawhebty.models.PostVisibility;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class PostSpecification {

    public static Specification<Post> hasStatus(PostStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            Join<Post, PostStatus> statusJoin = root.join("status", JoinType.INNER);
            return criteriaBuilder.equal(statusJoin.get("id"), status.getId());
        };
    }

    public static Specification<Post> hasVisibility(PostVisibility visibility) {
        return (root, query, criteriaBuilder) -> {
            if (visibility == null) return criteriaBuilder.conjunction();
            Join<Post, PostVisibility> visibilityJoin = root.join("visibility", JoinType.INNER);
            return criteriaBuilder.equal(visibilityJoin.get("id"), visibility.getId());
        };
    }

    public static Specification<Post> hasCategory(Integer categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Post> hasSubCategory(Integer subCategoryId) {
        return (root, query, criteriaBuilder) -> {
            if (subCategoryId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("subCategory").get("id"), subCategoryId);
        };
    }

    public static Specification<Post> hasOwner(Long ownerId) {
        return (root, query, criteriaBuilder) -> {
            if (ownerId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("ownerUser").get("id"), ownerId);
        };
    }

    public static Specification<Post> hasNoOwner(Long ownerId) {
        return (root, query, criteriaBuilder) -> {
            if (ownerId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.notEqual(root.get("ownerUser").get("id"), ownerId);
        };
    }

    public static Specification<Post> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("caption")), likePattern)
            );
        };
    }
}