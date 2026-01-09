package io.mawhebty.repository;

import io.mawhebty.enums.EventStatus;
import io.mawhebty.models.Event;
import io.mawhebty.models.TalentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /* ===================== BASIC ===================== */

    @Query("""
                SELECT e FROM Event e
                WHERE e.status = :status
                ORDER BY e.eventDate ASC
            """)
    Page<Event> findByStatus(@Param("status") EventStatus status, Pageable pageable);

    /* ===================== UPCOMING / PAST / ONGOING ===================== */

    @Query("""
                SELECT e FROM Event e
                WHERE e.eventDate >= :now
                AND e.status != :cancelled
                ORDER BY e.eventDate ASC
            """)
    Page<Event> findUpcomingEvents(
            @Param("now") LocalDateTime now,
            @Param("cancelled") EventStatus cancelled,
            Pageable pageable);

    @Query("""
                SELECT e FROM Event e
                WHERE e.getEndDate() < :now
                AND e.status != :cancelled
                ORDER BY e.eventDate DESC
            """)
    Page<Event> findPastEvents(
            @Param("now") LocalDateTime now,
            @Param("cancelled") EventStatus cancelled,
            Pageable pageable);

    @Query("""
            SELECT e FROM Event e
            WHERE e.eventDate <= :now
            AND COALESCE(e.endDate, e.eventDate) >= :now
            AND e.status = :ongoing
            ORDER BY e.eventDate ASC
            """)
    Page<Event> findOngoingEvents(
            @Param("now") LocalDateTime now,
            @Param("ongoing") EventStatus ongoing,
            Pageable pageable);

    /* ===================== FREE EVENTS ===================== */

    @Query("""
                SELECT e FROM Event e
                WHERE e.isFree = true
                AND e.status != :cancelled
                ORDER BY e.eventDate ASC
            """)
    Page<Event> findFreeEvents(
            @Param("cancelled") EventStatus cancelled,
            Pageable pageable);

    /* ===================== SEARCH ===================== */

    @Query("""
                SELECT e FROM Event e
                WHERE (
                    LOWER(e.titleEn) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.titleAr) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.descriptionEn) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.descriptionAr) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.tags) LIKE LOWER(CONCAT('%', :query, '%'))
                )
                AND e.status != :cancelled
                ORDER BY e.eventDate ASC
            """)
    Page<Event> searchEvents(
            @Param("query") String query,
            @Param("cancelled") EventStatus cancelled,
            Pageable pageable);

    /* ===================== DATE RANGE ===================== */

    @Query("""
                SELECT e FROM Event e
                WHERE e.eventDate BETWEEN :startDate AND :endDate
                AND e.status != :cancelled
                ORDER BY e.eventDate ASC
            """)
    Page<Event> findByEventDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("cancelled") EventStatus cancelled,
            Pageable pageable);

    /* ===================== COUNTS ===================== */

    Long countByStatus(EventStatus status);

    @Query("""
                SELECT COUNT(e) FROM Event e
                WHERE e.eventDate >= :now
                AND e.status != :cancelled
            """)
    Long countUpcomingEvents(
            @Param("now") LocalDateTime now,
            @Param("cancelled") EventStatus cancelled);

    @Query("""
                SELECT COUNT(e) FROM Event e
                WHERE e.isFree = true
                AND e.status != :cancelled
            """)
    Long countFreeEvents(@Param("cancelled") EventStatus cancelled);

    @Query("""
                SELECT COUNT(e) FROM Event e
                WHERE (
                    LOWER(e.titleEn) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.titleAr) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.descriptionEn) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.descriptionAr) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(e.tags) LIKE LOWER(CONCAT('%', :query, '%'))
                )
                AND e.status != :cancelled
            """)
    Long countBySearchQuery(
            @Param("query") String query,
            @Param("cancelled") EventStatus cancelled);

    /* ===================== CATEGORY ===================== */

    @Query("""
                SELECT e FROM Event e
                WHERE e.status != :cancelled
                AND e.category.id = :categoryId
                AND (:subCategoryId IS NULL OR e.subCategory.id = :subCategoryId)
                AND ( :search IS NULL OR LOWER(e.titleEn) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.titleAr) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.descriptionEn) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.descriptionAr) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.location) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.tags) LIKE LOWER(CONCAT('%', :search, '%')) )
                ORDER BY e.eventDate ASC
            """)
    Page<Event> findByCategoryAndSubCategory(
            @Param("categoryId") Integer categoryId,
            @Param("subCategoryId") Integer subCategoryId,
            @Param("cancelled") EventStatus cancelled,
            @Param("search") String search,
            Pageable pageable);

    /* ===================== MISC ===================== */

    List<Event> findTop5ByCategoryAndIdNotOrderByEventDateAsc(
            TalentCategory category,
            Long id);

    List<Event> findByIdInOrderByEventDateAsc(List<Long> ids);
}
