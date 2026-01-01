package io.mawhebty.repository;

import io.mawhebty.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Get events by status with pagination
    @Query(value = "SELECT * FROM events WHERE status = :status " +
            "ORDER BY event_date ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> findByStatusWithPagination(@Param("status") String status,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    // Get upcoming events
    @Query(value = "SELECT * FROM events WHERE event_date >= :now " +
            "AND status != 'CANCELLED' " +
            "ORDER BY event_date ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now,
                                   @Param("limit") int limit,
                                   @Param("offset") int offset);

    // Get past events
    @Query(value = "SELECT * FROM events WHERE event_date < :now " +
            "AND status != 'CANCELLED' " +
            "ORDER BY event_date DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> findPastEvents(@Param("now") LocalDateTime now,
                               @Param("limit") int limit,
                               @Param("offset") int offset);

    // Get ongoing events
    @Query(value = "SELECT * FROM events WHERE status = 'ONGOING' " +
            "ORDER BY event_date ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> findOngoingEvents(@Param("limit") int limit,
                                  @Param("offset") int offset);

    // Get free events
    @Query(value = "SELECT * FROM events WHERE is_free = true " +
            "AND status != 'CANCELLED' " +
            "ORDER BY event_date ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> findFreeEvents(@Param("limit") int limit,
                               @Param("offset") int offset);

    // Search events
    @Query(value = "SELECT * FROM events WHERE " +
            "(LOWER(title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(location) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND status != 'CANCELLED' " +
            "ORDER BY event_date ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Event> searchEvents(@Param("query") String query,
                             @Param("limit") int limit,
                             @Param("offset") int offset);

    // Count queries
    @Query(value = "SELECT COUNT(*) FROM events WHERE status = :status",
            nativeQuery = true)
    Long countByStatus(@Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM events WHERE event_date >= :now AND status != 'CANCELLED'",
            nativeQuery = true)
    Long countUpcomingEvents(@Param("now") LocalDateTime now);

    // Find events by IDs
    @Query(value = "SELECT * FROM events WHERE id IN :ids",
            nativeQuery = true)
    List<Event> findByIds(@Param("ids") List<Long> ids);
}