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
public class EventListItemResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;
    private String location;
    private String coverImageUrl;
}