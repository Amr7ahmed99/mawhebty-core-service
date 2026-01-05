package io.mawhebty.services;

import io.mawhebty.api.v1.resources.mawhebtyDashboard.*;
import io.mawhebty.dtos.responses.ArticleSummaryResponse;
import io.mawhebty.enums.ArticleStatusEnum;
import io.mawhebty.dtos.requests.CreateArticleRequest;
import io.mawhebty.dtos.responses.ArticleListResponse;
import io.mawhebty.models.Article;
import io.mawhebty.models.ArticleSection;
import io.mawhebty.models.TalentCategory;
import io.mawhebty.models.TalentSubCategory;
import io.mawhebty.repository.ArticleRepository;
import io.mawhebty.repository.TalentCategoryRepository;
import io.mawhebty.repository.TalentSubCategoryRepository;
import io.mawhebty.repository.specification.ArticleSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TalentCategoryRepository categoryRepository;
    private final TalentSubCategoryRepository subCategoryRepository;

    public ArticleListResponse getArticles(
            Integer categoryId,
            Integer subCategoryId,
            String search,
            Integer page,
            Integer perPage,
            String sortBy
    ) {

        Sort sort = Sort.by("publishedAt").descending();
        if ("date_asc".equals(sortBy)) {
            sort = Sort.by("publishedAt").ascending();
        }

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);

        Specification<Article> spec = Specification.allOf(
                ArticleSpecification.hasCategory(categoryId),
                ArticleSpecification.hasSubCategory(subCategoryId),
                ArticleSpecification.search(search)
        );

        Page<Article> pageResult = articleRepository.findAll(spec, pageable);

        return ArticleListResponse.builder()
                .totalItems(pageResult.getTotalElements())
                .currentPage(page)
                .perPage(perPage)
                .totalPages(pageResult.getTotalPages())
                .articles(
                        pageResult.getContent()
                                .stream()
                                .map(this::mapToListItem)
                                .toList()
                )
                .build();
    }

    private ArticleSummaryResponse mapToListItem(Article article) {
        Locale locale = LocaleContextHolder.getLocale();
        return ArticleSummaryResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .imageUrl(article.getCoverImageUrl())
                .categoryName("en".equals(locale.getLanguage())?
                        article.getCategory().getNameEn(): article.getCategory().getNameAr())
                .subCategoryName("en".equals(locale.getLanguage())?
                        article.getSubCategory().getNameEn(): article.getSubCategory().getNameAr())
                .publishedAt(article.getPublishedAt())
                .build();
    }

    public ArticleResponseResource createArticle(CreateArticleRequest request) {

        TalentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        TalentSubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Sub category not found"));

        Article article = Article.builder()
                .title(request.getTitle())
                .coverImageUrl(request.getImageUrl())
                .category(category)
                .subCategory(subCategory)
                .status(ArticleStatusEnum.valueOf(
                        request.getStatus() != null ? request.getStatus().name() : ArticleStatusEnum.PUBLISHED.name()
                ))
                .tags(request.getTags())
                .publishedAt(request.getPublishedAt() != null
                        ? request.getPublishedAt()
                        : null)
                .build();

        if (request.getSections() != null) {
            request.getSections().stream()
                    .sorted(Comparator.comparing(CreateArticleSectionResource::getSectionOrder))
                    .forEach(sectionRequest -> {
                        ArticleSection section = ArticleSection.builder()
                                .sectionOrder(sectionRequest.getSectionOrder())
                                .title(sectionRequest.getTitle())
                                .content(sectionRequest.getContent())
                                .imageUrl(sectionRequest.getImageUrl())
                                .videoUrl(sectionRequest.getVideoUrl())
                                .embedCode(sectionRequest.getEmbedCode())
                                .build();

                        article.addSection(section);
                    });
        }

        Article savedArticle = articleRepository.save(article);
        return mapToResponse(savedArticle);
    }

    public ArticleResponseResource updateArticle(
            Long articleId,
            UpdateArticleRequestResource request
    ) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        // -------- Basic fields --------
        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }

        if (request.getCoverImageUrl() != null) {
            article.setCoverImageUrl(request.getCoverImageUrl());
        }

        if (request.getStatus() != null) {
            article.setStatus(ArticleStatusEnum.valueOf(request.getStatus().name()));
        }

        if (request.getTags() != null) {
            article.setTags(request.getTags());
        }

        if (request.getPublishedAt() != null) {
            article.setPublishedAt(request.getPublishedAt());
        }

        // -------- Category --------
        if (request.getCategoryId() != null) {
            TalentCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            article.setCategory(category);
        }

        if (request.getSubCategoryId() != null) {
            TalentSubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Sub category not found"));
            article.setSubCategory(subCategory);
        }

        // -------- Sections --------
        if (request.getSections() != null) {

            // remove old sections (orphanRemoval = true)
            article.getSections().clear();

            request.getSections().stream()
                    .sorted(Comparator.comparing(UpdateArticleSectionResource::getSectionOrder))
                    .forEach(sectionReq -> {

                        ArticleSection section = ArticleSection.builder()
                                .sectionOrder(sectionReq.getSectionOrder())
                                .title(sectionReq.getTitle())
                                .content(sectionReq.getContent())
                                .imageUrl(sectionReq.getImageUrl())
                                .videoUrl(sectionReq.getVideoUrl())
                                .embedCode(sectionReq.getEmbedCode())
                                .build();

                        article.addSection(section);
                    });
        }

        Article updated = articleRepository.save(article);
        return mapToResponse(updated);
    }


    public void deleteArticle(Long articleId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        article.setStatus(ArticleStatusEnum.DELETED);
        articleRepository.save(article);
    }


    private ArticleResponseResource mapToResponse(Article article) {
        Locale locale = LocaleContextHolder.getLocale();

        ArticleResponseResource response = new ArticleResponseResource();
        response.setId(article.getId().intValue());
        response.setTitle(article.getTitle());
        response.setCoverImageUrl(article.getCoverImageUrl());
        response.setCategoryName("en".equals(locale.getLanguage())?
                article.getCategory().getNameEn(): article.getCategory().getNameAr());
        response.setSubCategoryName("en".equals(locale.getLanguage())?
                article.getSubCategory().getNameEn(): article.getSubCategory().getNameAr());
        response.setStatus(article.getStatus().name());
        response.setTags(article.getTags());
        response.setPublishedAt(
                article.getPublishedAt() != null
                        ? article.getPublishedAt()
                        : null
        );

        if (article.getSections() != null) {
            response.setSections(
                    article.getSections().stream().map(section -> {
                        ArticleSectionResponseResource r = new ArticleSectionResponseResource();
                        r.setId(section.getId().intValue());
                        r.setSectionOrder(section.getSectionOrder());
                        r.setTitle(section.getTitle());
                        r.setContent(section.getContent());
                        r.setImageUrl(section.getImageUrl());
                        r.setVideoUrl(section.getVideoUrl());
                        r.setEmbedCode(section.getEmbedCode());
                        return r;
                    }).toList()
            );
        }

        return response;
    }
}

