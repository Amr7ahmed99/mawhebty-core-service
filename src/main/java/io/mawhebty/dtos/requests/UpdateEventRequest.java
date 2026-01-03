package io.mawhebty.dtos.requests;

import io.mawhebty.enums.EventStatus;
import io.mawhebty.enums.EventType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    private String description;

    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;

    private LocalDateTime endDate;

    private String location;

    @Pattern(regexp = "^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?$",
            message = "Location coordinates must be in format 'latitude,longitude'")
    private String locationCoordinates;

    @URL(message = "Cover image URL must be valid")
    private String coverImageUrl;

    private EventStatus status;
    private EventType type;

    @Min(value = 1, message = "Max attendees must be at least 1")
    private Integer maxAttendees;

    private Boolean isFree;

    @DecimalMin(value = "0.0", message = "Ticket price cannot be negative")
    private Double ticketPrice;

    @URL(message = "Registration URL must be valid")
    private String registrationUrl;

    @URL(message = "Event link must be valid")
    private String eventLink;

    private String tags;
}
