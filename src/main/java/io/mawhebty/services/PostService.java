package io.mawhebty.services;

import io.mawhebty.api.v1.resources.mawhebtyPlatform.CategoryResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.PaginatedListResponseResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.PostListItemResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.PostListResponseResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.PostOwnerResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.PostWithRelatedResponseResource;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.SubcategoryResource;
import io.mawhebty.dtos.responses.*;
import io.mawhebty.enums.PostStatusEnum;
import io.mawhebty.enums.PostVisibilityEnum;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.repository.PostRepository;
import io.mawhebty.repository.PostStatusRepository;
import io.mawhebty.repository.PostVisibilityRepository;
import io.mawhebty.repository.specification.PostSpecification;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostStatusRepository postStatusRepository;
    private final PostVisibilityRepository postVisibilityRepository;
    private final MessageService messageService;
    private final UserProfileService userProfileService;

    public PaginatedListResponseResource getPosts(
            Integer categoryId,
            Integer subCategoryId,
            BigDecimal ownerId,
            Integer typeId,
            String search,
            Integer page,
            Integer perPage,
            String sortBy
    ) {
        // Get PUBLISHED status
        PostStatus publishedStatus = postStatusRepository.findByName(PostStatusEnum.PUBLISHED.getName())
                .orElseThrow(() -> new RuntimeException("Published status not found"));
        
        // Get PUBLIC visibility
        PostVisibility publicVisibility = postVisibilityRepository.findByName(PostVisibilityEnum.PUBLIC.getName())
                .orElseThrow(() -> new RuntimeException("Public visibility not found"));

        Sort sort = Sort.by("createdAt").descending();
        if (sortBy != null) {
            sort = sortBy.equals("created_asc")
                    ? Sort.by("createdAt").ascending()
                    : Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);

        // Build specification with mandatory filters
        Specification<Post> spec = Specification.allOf(
                PostSpecification.hasStatus(publishedStatus),
                PostSpecification.hasVisibility(publicVisibility),
                PostSpecification.hasCategory(categoryId),
                PostSpecification.hasSubCategory(subCategoryId),
                PostSpecification.hasOwner(ownerId!= null? ownerId.longValue(): null),
                PostSpecification.search(search)
        );

        Page<Post> postPage = postRepository.findAll(spec, pageable);

        PaginatedListResponseResource resource= new PaginatedListResponseResource();
        resource.setTotalItems(BigDecimal.valueOf(postPage.getTotalElements()));
        resource.setCurrentPage(page);
        resource.setPerPage(perPage);
        resource.setTotalPages(postPage.getTotalPages());
        resource.setData(
                postPage.getContent()
                        .stream()
                        .map(this::mapToPostListItemResponse)
                        .toList()
        );
        return resource;
    }

    public PaginatedListResponseResource getPostsByUserId(Long ownerId, Integer page, Integer perPage) {

        List<Post> posts = postRepository.findByOwnerIdWithPagination(ownerId, perPage, page);

        PaginatedListResponseResource resource= new PaginatedListResponseResource();
        resource.setTotalItems(BigDecimal.valueOf(posts.size()));
        resource.setCurrentPage(1);
        resource.setPerPage(posts.size());
        resource.setTotalPages(1);
        resource.setData(
                posts.stream()
                        .map(this::mapToPostListItemResponse)
                        .toList()
        );
        return resource;
    }

    public PostWithRelatedResponseResource getPostById(Long id) {
        // Get PUBLISHED status
        PostStatus publishedStatus = postStatusRepository.findByName(PostStatusEnum.PUBLISHED.getName())
                .orElseThrow(() -> new RuntimeException("Published status not found"));
        
        // Get PUBLIC visibility
        PostVisibility publicVisibility = postVisibilityRepository.findByName(PostVisibilityEnum.PUBLIC.getName())
                .orElseThrow(() -> new RuntimeException("Public visibility not found"));

        // Get the main post with PUBLISHED status and PUBLIC visibility
        Post post = postRepository.findByIdAndStatusAndVisibility(id, publishedStatus, publicVisibility)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found or not accessible (not published or not public) with id: " + id
                ));

        // Get 5 related posts based on category and subCategory, excluding the main post
        List<Post> relatedPosts = postRepository.findTop5ByCategoryAndIdNotAndStatusAndVisibilityOrderByCreatedAtDesc(
                post.getCategory(), 
                post.getId(), 
                publishedStatus, 
                publicVisibility
        );

        // Map main post
        PostWithRelatedResponseResource response = mapToPostWithRelatedResponse(post);

        // Map related posts
        List<PostListItemResource> related = relatedPosts.stream()
                .map(this::mapToPostListItemResponse)
                .toList();

        response.setRelatedPosts(related);

        return response;
    }

    private PostListItemResource mapToPostListItemResponse(Post post) {
        Locale locale = LocaleContextHolder.getLocale();

        PostListItemResource resource = new PostListItemResource();
        resource.setId(BigDecimal.valueOf(post.getId()));
        resource.setOwner(mapToPostOwner(post.getOwnerUser()));
        resource.setTitle(post.getTitle());
        resource.setCaption(post.getCaption());
        resource.setMediaUrl(post.getMediaUrl());
        resource.setDurationSeconds(post.getDurationSeconds());
        resource.setCategoryName(post.getCategory() != null ?
                ("en".equals(locale.getLanguage()) ? post.getCategory().getNameEn() : post.getCategory().getNameAr()) 
            : null);
        resource.setSubCategoryName(post.getSubCategory() != null? 
                ("en".equals(locale.getLanguage()) ? post.getSubCategory().getNameEn() : post.getSubCategory().getNameAr()) 
            : null);
        resource.setDate(post.getCreatedAt());
        resource.setIsSaved(false); // TODO: Placeholder, implement logic to check if saved
        resource.setLikesCount(0); // TODO: Placeholder, implement logic to get likes count
        return resource;
    }

    private PostWithRelatedResponseResource mapToPostWithRelatedResponse(Post post) {
        PostWithRelatedResponseResource resource = new PostWithRelatedResponseResource();
        resource.setId(BigDecimal.valueOf(post.getId()));
        resource.setOwner(mapToPostOwner(post.getOwnerUser()));
        // resource.setType(post.getType() != null ? post.getType().getName() : null);
        resource.setTitle(post.getTitle());
        resource.setCaption(post.getCaption());
        resource.setMediaUrl(post.getMediaUrl());
        resource.setDurationSeconds(post.getDurationSeconds());
        // resource.setVisibility(post.getVisibility() != null ? post.getVisibility().getName() : null);
        // resource.setStatus(post.getStatus() != null ? post.getStatus().getName() : null);
        resource.setCategoryName(post.getCategory() != null ? LocaleContextHolder.getLocale().getLanguage().equals("en") ?
                post.getCategory().getNameEn() : post.getCategory().getNameAr() : null);
        resource.setSubCategoryName(post.getSubCategory() != null ? LocaleContextHolder.getLocale().getLanguage().equals("en") ?
                post.getSubCategory().getNameEn() : post.getSubCategory().getNameAr() : null);
        resource.setDate(post.getCreatedAt());
        resource.setIsSaved(false); // TODO: Placeholder, implement logic to check if saved
        resource.setLikesCount(0); // TODO: Placeholder, implement logic to get likes count
        // resource.setCreatedAt(post.getCreatedAt());
        // resource.setUpdatedAt(post.getUpdatedAt());
        
        return resource;
    }

    private PostOwnerResource mapToPostOwner(User user) {
        PostOwnerResource resource = new PostOwnerResource();

        resource.setId(BigDecimal.valueOf(user.getId()));

        Object profile= this.userProfileService.getUserProfile(user);
        if (profile instanceof TalentProfile talentProfile) {
            resource.setFirstName(talentProfile.getFirstName());
            resource.setLastName(talentProfile.getLastName());
            resource.setImageUrl(talentProfile.getProfilePicture());
        }else if (profile instanceof IndividualResearcherProfile individualResearcherProfile){
            resource.setFirstName(individualResearcherProfile.getFirstName());
            resource.setLastName(individualResearcherProfile.getLastName());
            resource.setImageUrl(individualResearcherProfile.getProfilePicture());
        }else if (profile instanceof CompanyResearcherProfile companyResearcherProfile){
            resource.setFirstName(companyResearcherProfile.getCompanyName());
            resource.setImageUrl(companyResearcherProfile.getProfilePicture());
        }else {
            String s = profile != null ? profile.getClass().getName() : "null";
            log.warn(messageService.getMessage("unknown.profile.type",
                    new Object[]{s}));
            throw new IllegalStateException(messageService.getMessage("unknown.profile.type",
                    new Object[]{s}));
        }

        return resource;
    }

    private CategoryResource mapToCategory(TalentCategory category) {
        if (category == null) return null;
        Locale locale = LocaleContextHolder.getLocale();
        
        CategoryResource resource = new CategoryResource();
        resource.setId(category.getId());
        resource.setName("en".equals(locale.getLanguage()) ? category.getNameEn() : category.getNameAr());
        return resource;
    }

    private SubcategoryResource mapToSubCategory(TalentSubCategory subCategory) {
        if (subCategory == null) return null;
        Locale locale = LocaleContextHolder.getLocale();

        SubcategoryResource resource = new SubcategoryResource();
        resource.setId(subCategory.getId());
        resource.setName("en".equals(locale.getLanguage()) ? subCategory.getNameEn() : subCategory.getNameAr());
        return resource;
    }
}