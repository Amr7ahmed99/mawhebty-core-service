package io.mawhebty.controllers;

import io.mawhebty.api.v1.mawhebty.platform.AbstractMawhebtyPlatformController;
import io.mawhebty.api.v1.mawhebtyPlatform.MawhebtyPlatformUserSavedItemsApi;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.*;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.services.SavedItemService;
import io.mawhebty.services.auth.CurrentUserService;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController("MawhebtyPlatformUserSavedItemsController")
@RequiredArgsConstructor
public class UserSavedItemsController extends AbstractMawhebtyPlatformController
        implements MawhebtyPlatformUserSavedItemsApi {

    private final SavedItemService savedItemService;
    private final CurrentUserService currentUserService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<SavedItemsDataResource> getUserSavedItems(Integer itemType, Integer page, Integer perPage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            // Get current authenticated user
            User currentUser = currentUserService.getCurrentUser(authentication);

            // Validate pagination parameters
            if (page == null || page < 1) {
                page = 1;
            }
            if (perPage == null || perPage < 1) {
                perPage = 10;
            }
            if (perPage > 50) {
                perPage = 50;
            }

            int offset = (page - 1) * perPage;

            // Get saved items based on filter
            Map<String, Object> savedItemsData;
            if (itemType != null) {
                savedItemsData = savedItemService.getUserSavedItemsByType(
                        currentUser.getId(),
                        itemType,
                        perPage,
                        offset
                );
            }else {
                throw new BadDataException("item type is required");
            }

            // Build response
            SavedItemsDataResource response = buildResponse(savedItemsData);

            log.info(messageService.getMessage("saved.items.retrieved.success",
                    new Object[]{currentUser.getId(), itemType}));
            return ResponseEntity.ok().body(response);

        } catch (UserNotFoundException e) {
            log.error(messageService.getMessage("user.not.found"), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(messageService.getMessage("saved.items.retrieve.error"), e.getMessage(), e);
//            throw new RuntimeException(messageService.getMessage("saved.items.retrieve.error"), e);
            throw e;
        }
    }

    private SavedItemsDataResource buildResponse(Map<String, Object> savedItemsData) {
        // Build data
        SavedItemsDataResource response = new SavedItemsDataResource();

        // Add posts
        if (savedItemsData.containsKey("posts")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> posts = (List<Map<String, Object>>) savedItemsData.get("posts");
            PaginatedListResponseResource paginatedListResponseResource = new PaginatedListResponseResource();
            paginatedListResponseResource.setTotalItems(BigDecimal.valueOf((Long) savedItemsData.get("post_total_items")));
            paginatedListResponseResource.setTotalPages((Integer) savedItemsData.get("post_total_pages"));
            paginatedListResponseResource.setCurrentPage((Integer) savedItemsData.get("post_current_page"));
            paginatedListResponseResource.setPerPage((Integer) savedItemsData.get("post_per_page"));
            paginatedListResponseResource.setData(mapToSavedPostResources(posts));
            response.setPosts(paginatedListResponseResource);
        }

        // Add events
        if (savedItemsData.containsKey("events")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> events = (List<Map<String, Object>>) savedItemsData.get("events");
            PaginatedListResponseResource paginatedListResponseResource = new PaginatedListResponseResource();
            paginatedListResponseResource.setTotalItems(BigDecimal.valueOf((Long) savedItemsData.get("event_total_items")));
            paginatedListResponseResource.setTotalPages((Integer) savedItemsData.get("event_total_pages"));
            paginatedListResponseResource.setCurrentPage((Integer) savedItemsData.get("event_current_page"));
            paginatedListResponseResource.setPerPage((Integer) savedItemsData.get("event_per_page"));
            paginatedListResponseResource.setData(mapToSavedEventResources(events));
            response.setEvents(paginatedListResponseResource);
        }

        // Add articles
        if (savedItemsData.containsKey("articles")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> articles = (List<Map<String, Object>>) savedItemsData.get("articles");
            PaginatedListResponseResource paginatedListResponseResource = new PaginatedListResponseResource();
            paginatedListResponseResource.setTotalItems(BigDecimal.valueOf((Long) savedItemsData.get("article_total_items")));
            paginatedListResponseResource.setTotalPages((Integer) savedItemsData.get("article_total_pages"));
            paginatedListResponseResource.setCurrentPage((Integer) savedItemsData.get("article_current_page"));
            paginatedListResponseResource.setPerPage((Integer) savedItemsData.get("article_per_page"));
            paginatedListResponseResource.setData(mapToSavedArticleResources(articles));
            response.setArticles(paginatedListResponseResource);
        }

        return response;
    }

    private List<SavedPostResource> mapToSavedPostResources(List<Map<String, Object>> posts) {
        List<SavedPostResource> postResources = new ArrayList<>();

        for (Map<String, Object> postData : posts) {
            SavedPostResource postResource = new SavedPostResource();
            postResource.setId(((Long) postData.get("id")).intValue());
            
            // Set owner
            @SuppressWarnings("unchecked")
            Map<String, Object> ownerData = (Map<String, Object>) postData.get("owner");
            PostOwnerResource ownerResource = new PostOwnerResource();
            ownerResource.setId(BigDecimal.valueOf((Long) ownerData.get("id")));
            ownerResource.setFirstName((String) ownerData.get("first_name"));
            ownerResource.setLastName((String) ownerData.get("last_name"));
            ownerResource.setImageUrl((String) ownerData.get("image_url"));
            postResource.setOwner(ownerResource);

            postResource.setTitle((String) postData.get("title"));
            postResource.setCaption((String) postData.get("caption"));
            postResource.setImageUrl((String) postData.get("image_url"));
            postResource.setDate(LocalDateTime.parse((String) postData.get("date")));
            postResource.setSavedAt(LocalDateTime.parse((String)  postData.get("saved_at")));
            postResource.setCategoryName((String) postData.get("category_name"));
            postResource.setSubCategoryName((String) postData.get("sub_category_name"));
            postResource.setIsSaved((boolean) postData.get("is_saved"));
            postResource.setLikesCount((Integer) postData.get("likes_count"));
            postResources.add(postResource);
        }

        return postResources;
    }

    private List<SavedEventResource> mapToSavedEventResources(List<Map<String, Object>> events) {
        List<SavedEventResource> eventResources = new ArrayList<>();

        for (Map<String, Object> eventData : events) {
            SavedEventResource eventResource = new SavedEventResource();
            eventResource.setId(((Long) eventData.get("id")).intValue());
            eventResource.setTitle((String) eventData.get("title"));
            eventResource.setDescription((String) eventData.get("description"));
            eventResource.setLocation((String) eventData.get("location"));
            eventResource.setLocationCoordinates((String) eventData.get("location_coordinates"));
            eventResource.setImageUrl((String) eventData.get("image_url"));
            eventResource.setDate(LocalDateTime.parse((String) eventData.get("date")));
            eventResource.setSavedAt(LocalDateTime.parse((String)  eventData.get("saved_at")));

            eventResources.add(eventResource);
        }

        return eventResources;
    }

    private List<SavedArticleResource> mapToSavedArticleResources(List<Map<String, Object>> articles) {
        List<SavedArticleResource> articleResources = new ArrayList<>();

        for (Map<String, Object> articleData : articles) {
            SavedArticleResource articleResource = new SavedArticleResource();
            articleResource.setId(((Long) articleData.get("id")).intValue());
            articleResource.setTitle((String) articleData.get("title"));
            articleResource.setDescription((String) articleData.get("description"));
            articleResource.setImageUrl((String) articleData.get("image_url"));
            articleResource.setCategoryName((String) articleData.get("category_name"));
            articleResource.setSubCategoryName((String) articleData.get("sub_category_name"));
            articleResource.setDate(LocalDateTime.parse((String) articleData.get("date")));
            articleResource.setSavedAt(LocalDateTime.parse((String)  articleData.get("saved_at")));

            articleResources.add(articleResource);
        }

        return articleResources;
    }
}