package io.mawhebty.controllers;

import io.mawhebty.api.v1.mawhebty.platform.AbstractMawhebtyPlatformController;
import io.mawhebty.api.v1.mawhebtyPlatform.MawhebtyPlatformHomeSectionsApi;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.*;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.models.User;
import io.mawhebty.services.UserHomeService;
import io.mawhebty.services.auth.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController("MawhebtyPlatformUserHomeController")
@RequiredArgsConstructor
public class UserHomeController extends AbstractMawhebtyPlatformController
        implements MawhebtyPlatformHomeSectionsApi {

    private final UserHomeService userHomeService;
    private final CurrentUserService currentUserService;

    @Override
    public ResponseEntity<HomeSectionsDataResource> getHomeSections(BigDecimal perSectionItemsCount, String search) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = currentUserService.getCurrentUser(authentication);

            // Get saved items based on filter
            Map<String, Object> homeSectionsData= userHomeService.getHomeSections(
                    currentUser.getId(),
                    this.pageOf(0, perSectionItemsCount.intValue(), Sort.by("id").descending()),
                    search
            );

            // Build response
            HomeSectionsDataResource response = buildResponse(homeSectionsData);
            return ResponseEntity.ok().body(response);

        } catch (UserNotFoundException e) {
            log.error("user not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch home sections: {}", e.getMessage(), e);
            throw e;
        }
    }

    private HomeSectionsDataResource buildResponse(Map<String, Object> homeSectionsData) {
        // Build data
        HomeSectionsDataResource response = new HomeSectionsDataResource();

        // Add posts
        if (homeSectionsData.containsKey("posts")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> posts = (List<Map<String, Object>>) homeSectionsData.get("posts");
            response.setPosts(mapToPostSectionResources(posts));
        }

        // Add events
        if (homeSectionsData.containsKey("events")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> events = (List<Map<String, Object>>) homeSectionsData.get("events");
            response.setEvents(mapToEventSectionResources(events));
        }

        // Add articles
        if (homeSectionsData.containsKey("articles")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> articles = (List<Map<String, Object>>) homeSectionsData.get("articles");
            response.setArticles(mapToArticleSectionResources(articles));
        }

        return response;
    }

    private List<PostSectionResource> mapToPostSectionResources(List<Map<String, Object>> posts) {
        List<PostSectionResource> postSectionResource = new ArrayList<>();

        for (Map<String, Object> postData : posts) {
            PostSectionResource postResource = new PostSectionResource();
            postResource.setId(BigDecimal.valueOf((Long) postData.get("id")));

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
            postResource.setCategoryName((String) postData.get("category_name"));
            postResource.setSubCategoryName((String) postData.get("sub_category_name"));
            postResource.setDate(LocalDateTime.parse((String) postData.get("date")));
            postResource.setIsSaved((Boolean) postData.get("is_saved"));
            postSectionResource.add(postResource);
        }

        return postSectionResource;
    }

    private List<EventSectionResource> mapToEventSectionResources(List<Map<String, Object>> events) {
        List<EventSectionResource> eventResources = new ArrayList<>();

        for (Map<String, Object> eventData : events) {
            EventSectionResource eventResource = new EventSectionResource();
            eventResource.setId(BigDecimal.valueOf((Long) eventData.get("id")));
            eventResource.setTitle((String) eventData.get("title"));
            eventResource.setDescription((String) eventData.get("description"));
            eventResource.setLocation((String) eventData.get("location"));
            eventResource.setLocationCoordinates((String) eventData.get("location_coordinates"));
            eventResource.setImageUrl((String) eventData.get("image_url"));
            eventResource.setDate(LocalDateTime.parse((String) eventData.get("date")));
            eventResources.add(eventResource);
        }

        return eventResources;
    }

    private List<ArticleSectionResource> mapToArticleSectionResources(List<Map<String, Object>> articles) {
        List<ArticleSectionResource> articleResources = new ArrayList<>();

        for (Map<String, Object> articleData : articles) {
            ArticleSectionResource articleResource = new ArticleSectionResource();
            articleResource.setId(BigDecimal.valueOf((Long) articleData.get("id")));
            articleResource.setTitle((String) articleData.get("title"));
            articleResource.setImageUrl((String) articleData.get("image_url"));
            articleResource.setCategoryName((String) articleData.get("category_name"));
            articleResource.setSubCategoryName((String) articleData.get("sub_category_name"));
            articleResource.setDate(LocalDateTime.parse((String) articleData.get("date")));
            articleResources.add(articleResource);
        }

        return articleResources;
    }
}