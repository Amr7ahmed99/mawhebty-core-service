package io.mawhebty.services;

import io.mawhebty.config.LocaleConfig;
import io.mawhebty.dtos.responses.ArticleSummaryResponse;
import io.mawhebty.dtos.responses.ArticleListResponse;
import io.mawhebty.models.Article;
import io.mawhebty.repository.ArticleRepository;
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

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;

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
}

