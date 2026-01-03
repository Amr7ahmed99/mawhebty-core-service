package io.mawhebty.dtos.responses;

import io.mawhebty.enums.EventStatus;
import io.mawhebty.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime endDate;
    private String location;
    private String locationCoordinates;
    private String coverImageUrl;
    private EventStatus status;
    private EventType type;
    private Integer maxAttendees;
    private Integer currentAttendees;
    private Boolean isFree;
    private Double ticketPrice;
    private String registrationUrl;
    private String eventLink;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer categoryId;
    private Integer subCategoryId;
}
