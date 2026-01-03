package io.mawhebty.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListResponse {
    private Long total;
    private Integer page;
    private Integer perPage;
    private Integer totalPages;
    private List<EventListItemResponse> events;
}