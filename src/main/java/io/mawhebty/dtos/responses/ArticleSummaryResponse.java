package io.mawhebty.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ArticleSummaryResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private String description;
    private String categoryName;
    private String subCategoryName;
    private String status;
}
