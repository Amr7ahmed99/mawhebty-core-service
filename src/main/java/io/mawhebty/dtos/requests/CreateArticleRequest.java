package io.mawhebty.dtos.requests;

import io.mawhebty.api.v1.resources.mawhebtyDashboard.CreateArticleSectionResource;
import io.mawhebty.enums.ArticleStatusEnum;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateArticleRequest {

    private String title;
    private String imageUrl;

    private Integer categoryId;
    private Integer subCategoryId;

    private ArticleStatusEnum status;
    private String tags;

    private LocalDateTime publishedAt;

    private List<CreateArticleSectionResource> sections;
}
