package io.mawhebty.dtos.responses;

import io.mawhebty.api.v1.resources.mawhebtyPlatform.EventSummaryResource;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class EventWithRelatedResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;
    private LocalDateTime endDate;
    private String location;
    private String locationCoordinates;
    private String imageUrl;
    private String status;
    private String type;
    private Integer maxAttendees;
    private Integer currentAttendees;
    private Boolean isFree;
    private Double ticketPrice;
    private String registrationUrl;
    private String eventLink;
    private String tags;

    private List<EventSummaryResource> relatedEvents;
}
