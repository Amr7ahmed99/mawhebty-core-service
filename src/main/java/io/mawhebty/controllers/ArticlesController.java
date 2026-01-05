package io.mawhebty.controllers;

import io.mawhebty.api.v1.mawhebty.platform.AbstractMawhebtyPlatformController;
import io.mawhebty.api.v1.mawhebtyPlatform.ArticlesApi;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.ArticleListResponseResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.ArticleResponseResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.ArticleSummaryResource;
import io.mawhebty.dtos.responses.ArticleListResponse;
import io.mawhebty.dtos.responses.ArticleSummaryResponse;
import io.mawhebty.services.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController("ArticlesController")
@RequiredArgsConstructor
public class ArticlesController extends AbstractMawhebtyPlatformController
        implements ArticlesApi {

    private final ArticleService articleService;

    @Override
    public ResponseEntity<ArticleListResponseResource> getArticles(
            Integer categoryId,
            Integer subCategoryId,
            String search,
            Integer page,
            Integer perPage,
            String sortBy
    ) {

        if (page == null || page < 1) page = 1;
        if (perPage == null || perPage < 1) perPage = 10;
        if (perPage > 50) perPage = 50;

        // Service returns ArticleListResponse from OpenAPI spec
        ArticleListResponse response = articleService.getArticles(
                categoryId,
                subCategoryId,
                search,
                page,
                perPage,
                sortBy
        );

        List<ArticleSummaryResource> articleResources = response.getArticles()
                .stream()
                .map(this::mapToArticleSummaryResource)
                .collect(Collectors.toList());

        // Map to Resource for API response
        ArticleListResponseResource resource = new ArticleListResponseResource();
        resource.setTotalItems(BigDecimal.valueOf(response.getTotalItems()));
        resource.setCurrentPage(response.getCurrentPage());
        resource.setPerPage(response.getPerPage());
        resource.setTotalPages(response.getTotalPages());
        resource.setArticles(articleResources);
        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<ArticleResponseResource> getArticleById(Integer id) {

        ArticleResponseResource response =
                articleService.getArticleById(id.longValue());

        return ResponseEntity.ok(response);
    }

    private ArticleSummaryResource mapToArticleSummaryResource(ArticleSummaryResponse summary) {
        ArticleSummaryResource resource = new ArticleSummaryResource();
        resource.setId(BigDecimal.valueOf(summary.getId()));
        resource.setTitle(summary.getTitle());
        resource.setImageUrl(summary.getImageUrl());
        resource.setCategoryName(summary.getCategoryName());
        resource.setSubCategoryName(summary.getSubCategoryName());
        resource.setPublishedAt(summary.getPublishedAt());
        return resource;
    }


}