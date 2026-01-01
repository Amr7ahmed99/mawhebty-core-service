package io.mawhebty.models;

import io.mawhebty.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_attendees",
        indexes = {
                @Index(name = "idx_attendee_event", columnList = "event_id"),
                @Index(name = "idx_attendee_user", columnList = "user_id"),
                @Index(name = "idx_attendee_unique", columnList = "event_id, user_id", unique = true)
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.REGISTERED;

    @Column(name = "registered_at", nullable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    @Column(name = "ticket_number")
    private String ticketNumber;

    public LocalDateTime getAttendedAt() {
        return attendedAt != null ? attendedAt : (event != null ? event.getEventDate() : null);
    }
}