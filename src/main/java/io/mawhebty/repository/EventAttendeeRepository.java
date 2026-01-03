package io.mawhebty.repository;

import io.mawhebty.models.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {

    // Get attendees by event with pagination
    @Query(value = "SELECT * FROM event_attendees WHERE event_id = :eventId " +
            "ORDER BY registered_at ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<EventAttendee> findByEventIdWithPagination(@Param("eventId") Long eventId,
                                                    @Param("limit") int limit,
                                                    @Param("offset") int offset);

    // Get registered attendees by event
    @Query(value = "SELECT * FROM event_attendees WHERE event_id = :eventId " +
            "AND status = 'REGISTERED' " +
            "ORDER BY registered_at ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<EventAttendee> findRegisteredAttendeesByEventId(@Param("eventId") Long eventId,
                                                         @Param("limit") int limit,
                                                         @Param("offset") int offset);

    // Get user's event attendances with pagination
    @Query(value = "SELECT ea.* FROM event_attendees ea " +
            "LEFT JOIN events e ON ea.event_id = e.id " +
            "WHERE ea.user_id = :userId " +
            "ORDER BY e.event_date DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<EventAttendee> findByUserIdWithPagination(@Param("userId") Long userId,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);

    // Get user's upcoming registered events
    @Query(value = "SELECT ea.* FROM event_attendees ea " +
            "LEFT JOIN events e ON ea.event_id = e.id " +
            "WHERE ea.user_id = :userId " +
            "AND ea.status = 'REGISTERED' " +
            "AND e.status != 'CANCELLED' " +
            "AND e.event_date >= :now " +
            "ORDER BY e.event_date ASC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<EventAttendee> findUpcomingRegisteredEventsByUserId(@Param("userId") Long userId,
                                                             @Param("now") LocalDateTime now,
                                                             @Param("limit") int limit,
                                                             @Param("offset") int offset);

    // Get user's past attended events
    @Query(value = "SELECT ea.* FROM event_attendees ea " +
            "LEFT JOIN events e ON ea.event_id = e.id " +
            "WHERE ea.user_id = :userId " +
            "AND ea.status = 'ATTENDED' " +
            "AND e.event_date < :now " +
            "ORDER BY e.event_date DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<EventAttendee> findPastAttendedEventsByUserId(@Param("userId") Long userId,
                                                       @Param("now") LocalDateTime now,
                                                       @Param("limit") int limit,
                                                       @Param("offset") int offset);

    // Count queries
    @Query(value = "SELECT COUNT(*) FROM event_attendees WHERE event_id = :eventId AND status = 'REGISTERED'",
            nativeQuery = true)
    Long countRegisteredAttendeesByEventId(@Param("eventId") Long eventId);

    @Query(value = "SELECT COUNT(*) FROM event_attendees WHERE event_id = :eventId AND status = 'ATTENDED'",
            nativeQuery = true)
    Long countAttendedAttendeesByEventId(@Param("eventId") Long eventId);

    @Query(value = "SELECT COUNT(*) FROM event_attendees WHERE user_id = :userId",
            nativeQuery = true)
    Long countByUserId(@Param("userId") Long userId);

    // Find specific attendee
    @Query(value = "SELECT * FROM event_attendees WHERE event_id = :eventId AND user_id = :userId",
            nativeQuery = true)
    Optional<EventAttendee> findByEventIdAndUserId(@Param("eventId") Long eventId,
                                                   @Param("userId") Long userId);

    // Check if user is attending
    @Query(value = "SELECT COUNT(*) > 0 FROM event_attendees WHERE event_id = :eventId AND user_id = :userId",
            nativeQuery = true)
    boolean existsByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
}