package io.mawhebty.repository;

import io.mawhebty.enums.EventStatus;
import io.mawhebty.models.Event;
import io.mawhebty.models.Post;
import io.mawhebty.models.TalentCategory;
import io.mawhebty.models.TalentSubCategory;
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

    // Get events by status with pagination using Pageable
    @Query("SELECT e FROM Event e WHERE e.status = :status ORDER BY e.eventDate ASC")
    Page<Event> findByStatus(@Param("status") EventStatus status, Pageable pageable);

    // Get upcoming events
    @Query("SELECT e FROM Event e WHERE e.eventDate >= :now AND e.status != 'CANCELLED' ORDER BY e.eventDate ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    // Get past events
    @Query("SELECT e FROM Event e WHERE e.eventDate < :now AND e.status != 'CANCELLED' ORDER BY e.eventDate DESC")
    Page<Event> findPastEvents(@Param("now") LocalDateTime now, Pageable pageable);

    // Get ongoing events
    @Query("SELECT e FROM Event e WHERE e.status = 'ONGOING' ORDER BY e.eventDate ASC")
    Page<Event> findOngoingEvents(Pageable pageable);

    // Get free events
    @Query("SELECT e FROM Event e WHERE e.isFree = true AND e.status != 'CANCELLED' ORDER BY e.eventDate ASC")
    Page<Event> findFreeEvents(Pageable pageable);

    // Search events
    @Query("SELECT e FROM Event e WHERE " +
            "(LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND e.status != 'CANCELLED' " +
            "ORDER BY e.eventDate ASC")
    Page<Event> searchEvents(@Param("query") String query, Pageable pageable);

    // Events between dates
    @Query("SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate AND e.status != 'CANCELLED' ORDER BY e.eventDate ASC")
    Page<Event> findByEventDateBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    // Count queries
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.eventDate >= :now AND e.status != 'CANCELLED'")
    Long countUpcomingEvents(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.isFree = true AND e.status != 'CANCELLED'")
    Long countFreeEvents();

    @Query("SELECT COUNT(e) FROM Event e WHERE " +
            "(LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(e.tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND e.status != 'CANCELLED'")
    Long countBySearchQuery(@Param("query") String query);

    // Find events by IDs
    @Query("SELECT e FROM Event e WHERE e.id IN :ids ORDER BY e.eventDate ASC")
    List<Event> findByIds(@Param("ids") List<Long> ids);

    List<Event> findTop5ByCategoryAndIdNot(
            TalentCategory category, Long id);

    @Query("""
        SELECT e
        FROM Event e
        WHERE e.status != 'CANCELLED'
        AND e.category.id = :categoryId 
        AND (:subCategoryId IS NULL OR e.subCategory.id = :subCategoryId)
    """)
    Page<Event> findByCategoryIdSubCategoryId(
            @Param("categoryId") Integer categoryId,
            @Param("subCategoryId") Integer subCategoryId,
            Pageable pageable
    );

}