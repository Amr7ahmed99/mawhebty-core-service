package io.mawhebty.controllers.internalServices;

import io.mawhebty.api.v1.mawhebty.dashboard.AbstractMawhebtyDashboardController;
import io.mawhebty.api.v1.mawhebtyDashboard.ArticlesDashboardApi;
import io.mawhebty.api.v1.resources.mawhebtyDashboard.ArticleDashboardResponseResource;
import io.mawhebty.api.v1.resources.mawhebtyDashboard.CreateArticleRequestResource;
import io.mawhebty.api.v1.resources.mawhebtyDashboard.UpdateArticleRequestResource;
import io.mawhebty.dtos.requests.CreateArticleRequest;
import io.mawhebty.enums.ArticleStatusEnum;
import io.mawhebty.services.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("ArticlesDashboardController")
@RequiredArgsConstructor
public class ArticlesController extends AbstractMawhebtyDashboardController
        implements ArticlesDashboardApi {

    private final ArticleService articleService;

    @Override
    public ResponseEntity<ArticleDashboardResponseResource> createArticle(CreateArticleRequestResource requestResource) {

        ArticleDashboardResponseResource response =
                articleService.createArticle(mapToCreateArticleRequest(requestResource));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToArticleResponseResource(response));
    }

    @Override
    public ResponseEntity<ArticleDashboardResponseResource> updateArticle(
            Integer id,
            UpdateArticleRequestResource request
    ) {
        return ResponseEntity.ok(
                articleService.updateArticle(
                        id.longValue(),
                        request
                )
        );
    }

    @Override
    public ResponseEntity<Void> deleteArticle(Integer id) {
        articleService.deleteArticle(id.longValue());
        return ResponseEntity.noContent().build();
    }

    private CreateArticleRequest mapToCreateArticleRequest(CreateArticleRequestResource r) {
        CreateArticleRequest req = new CreateArticleRequest();
        req.setTitleEn(r.getTitleEn());
        req.setTitleAr(r.getTitleAr());
        req.setImageUrl(r.getImageUrl());
        req.setCategoryId(r.getCategoryId());
        req.setSubCategoryId(r.getSubCategoryId());
        req.setStatus(ArticleStatusEnum.valueOf(r.getStatus().getValue()));
        req.setTags(r.getTags());
        req.setPublishedAt(r.getPublishedAt());
        req.setSections(r.getSections());
        return req;
    }
    private ArticleDashboardResponseResource mapToArticleResponseResource(ArticleDashboardResponseResource r) {
        ArticleDashboardResponseResource resource = new ArticleDashboardResponseResource();
        resource.setId(r.getId());
        resource.setTitleEn(r.getTitleEn());
        resource.setTitleAr(r.getTitleAr());
        resource.setCoverImageUrl(r.getCoverImageUrl());
        resource.setCategoryName(r.getCategoryName());
        resource.setSubCategoryName(r.getSubCategoryName());
        resource.setStatus(r.getStatus());
        resource.setTags(r.getTags());
        resource.setPublishedAt(r.getPublishedAt());
        resource.setSections(r.getSections());
        return resource;
    }
}