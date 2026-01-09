package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.mawhebty.enums.EventStatus;
import io.mawhebty.enums.EventType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events",
        indexes = {
                @Index(name = "idx_event_status", columnList = "status"),
                @Index(name = "idx_event_category", columnList = "category_id"),
                @Index(name = "idx_event_sub_category", columnList = "sub_category_id"),
                @Index(name = "idx_event_date", columnList = "event_date"),
                @Index(name = "idx_event_created", columnList = "createdAt")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_en", nullable = false)
    private String titleEn;
    @Column(name = "title_ar", nullable = false)
    private String titleAr;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;
    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String location;

    @Column(name = "location_coordinates")
    private String locationCoordinates; // Format: "latitude,longitude"

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventType type = EventType.GENERAL;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @Column(name = "current_attendees")
    @Builder.Default
    private Integer currentAttendees = 0;

    @Column(name = "is_free")
    @Builder.Default
    private Boolean isFree = true;

    @Column(name = "ticket_price")
    private Double ticketPrice;

    @Column(name = "registration_url")
    private String registrationUrl;

    @Column(name = "event_link")
    private String eventLink; // For online events

    @Column(name = "tags", length = 1000)
    private String tags; // Comma-separated tags

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private TalentCategory category;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    @JsonIgnore
    private TalentSubCategory subCategory;


    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventAttendee> attendees = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SavedItem> savedByUsers = new ArrayList<>();

    public LocalDateTime getEndDate() {
        return endDate != null ? endDate : eventDate;
    }
}