package io.mawhebty.repository.specification;

import io.mawhebty.enums.EventStatus;
import io.mawhebty.enums.EventType;
import io.mawhebty.models.Event;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class EventSpecification {

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> hasNoStatus(EventStatus status) {
        return (root, query, cb) -> status == null ? null : cb.notEqual(root.get("status"), status);
    }

    public static Specification<Event> isFree(Boolean isFree) {
        return (root, query, cb) -> isFree == null ? null : cb.equal(root.get("isFree"), isFree);
    }

    public static Specification<Event> hasType(EventType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Event> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank())
                return null;

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("titleEn")), like),
                    cb.like(cb.lower(root.get("titleAr")), like),
                    cb.like(cb.lower(root.get("descriptionEn")), like),
                    cb.like(cb.lower(root.get("descriptionAr")), like),
                    cb.like(cb.lower(root.get("location")), like));
        };
    }

    public static Specification<Event> betweenDates(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null)
                return null;
            if (start != null && end != null)
                return cb.between(root.get("eventDate"), start, end);
            if (start != null)
                return cb.greaterThanOrEqualTo(root.get("eventDate"), start);
            return cb.lessThanOrEqualTo(root.get("eventDate"), end);
        };
    }

    public static Specification<Event> hasCategory(Integer categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Event> hasSubCategory(Integer subCategoryId) {
        return (root, query, cb) -> subCategoryId == null ? null
                : cb.equal(root.get("subCategory").get("id"), subCategoryId);
    }
}
