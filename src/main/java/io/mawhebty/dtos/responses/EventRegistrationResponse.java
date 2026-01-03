package io.mawhebty.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationResponse {
    private Boolean success;
    private String message;
    private Long registrationId;
    private Long eventId;
    private Long userId;
    private LocalDateTime registeredAt;
}