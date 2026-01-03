package io.mawhebty.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ArticleListResponse {
    private Long totalItems;
    private Integer currentPage;
    private Integer perPage;
    private Integer totalPages;
    private List<ArticleSummaryResponse> articles;
}
