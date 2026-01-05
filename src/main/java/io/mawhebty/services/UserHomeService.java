package io.mawhebty.services;

import io.mawhebty.enums.PostStatusEnum;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHomeService {

    private final PostRepository postRepository;
    private final EventRepository eventRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final UserProfileService userProfileService;

    @Transactional(readOnly = true)
    public Map<String, Object> getHomeSections(Long userId, Pageable pageable) {

        Map<String, Object> result = new HashMap<>();

        User user= this.userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                messageService.getMessage("user.not.found.id",
                        new Object[]{userId})
        ));

        Object profile= this.userProfileService.getUserProfile(user);
        TalentCategory category;
        TalentSubCategory subCategory;
        if (profile instanceof TalentProfile tp) {
            category= tp.getCategory();
            subCategory= tp.getSubCategory();
        }else if (profile instanceof ResearcherProfile rp){
            category= rp.getCategory();
            subCategory= rp.getSubCategory();
        }else {
            throw new IllegalStateException("No profile found for this user");
        }

        Page<Post> posts = postRepository.findPublicByOwnerIdAndCategoryIdSubCategoryId(userId, PostStatusEnum.PUBLISHED.getId(), category.getId(),
                subCategory!=null? subCategory.getId(): null, pageable);
        result.put("posts", mapToPostResponse(posts.getContent()));

        Page<Event> events = eventRepository.findByCategoryIdSubCategoryId(category.getId(),
                subCategory!=null? subCategory.getId(): null, pageable);
        result.put("events", mapToEventResponse(events.getContent()));

        Page<Article> articles = articleRepository.findByCategoryIdSubCategoryId(category.getId(),
                subCategory!=null? subCategory.getId(): null, pageable);
        result.put("articles", mapToArticleResponse(articles.getContent()));

        return result;
    }

    private List<Map<String, Object>> mapToPostResponse(List<Post> posts) {
        return posts.stream().map(post -> {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", post.getId());
            postMap.put("owner", this.getOwnerInfo(post.getOwnerUser()));
            postMap.put("title", post.getTitle());
            postMap.put("description", post.getCaption());
            postMap.put("image_url", post.getMediaUrl());
            postMap.put("date", post.getCreatedAt().toString());
            return postMap;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapToEventResponse(List<Event> events) {
        return events.stream().map(event -> {
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("title", event.getTitle());
            eventMap.put("description", event.getDescription());
            eventMap.put("location", event.getLocation());
            eventMap.put("location_coordinates", event.getLocationCoordinates());
            eventMap.put("image_url", event.getCoverImageUrl());
            eventMap.put("date", event.getEventDate().toString());
            return eventMap;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapToArticleResponse(List<Article> articles) {
        Locale locale = LocaleContextHolder.getLocale();
        return articles.stream().map(article -> {
            Map<String, Object> articleMap = new HashMap<>();
            articleMap.put("id", article.getId());
            articleMap.put("title", article.getTitle());
            articleMap.put("date", article.getPublishedAt() != null ?
                    article.getPublishedAt().toString() : article.getCreatedAt().toString());
            articleMap.put("image_url", article.getCoverImageUrl());
            articleMap.put("category_name", "en".equals(locale.getLanguage())?
                    article.getCategory().getNameEn(): article.getCategory().getNameAr());
            articleMap.put("sub_category_name", "en".equals(locale.getLanguage())?
                    article.getSubCategory().getNameEn(): article.getSubCategory().getNameAr());
            return articleMap;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> getOwnerInfo(User user) {
        Map<String, Object> owner = new HashMap<>();
        owner.put("id", user.getId());

        Object profile= this.userProfileService.getUserProfile(user);
        if (profile instanceof TalentProfile talentProfile) {
            owner.put("first_name", talentProfile.getFirstName());
            owner.put("last_name", talentProfile.getLastName());
            owner.put("image_url", talentProfile.getProfilePicture());
        }else if (profile instanceof IndividualResearcherProfile individualResearcherProfile){
            owner.put("first_name", individualResearcherProfile.getFirstName());
            owner.put("last_name", individualResearcherProfile.getLastName());
            owner.put("image_url", individualResearcherProfile.getProfilePicture());
        }else if (profile instanceof CompanyResearcherProfile companyResearcherProfile){
            owner.put("first_name", companyResearcherProfile.getCompanyName());
            owner.put("image_url", companyResearcherProfile.getProfilePicture());
        }else {
            String s = profile != null ? profile.getClass().getName() : "null";
            log.warn(messageService.getMessage("unknown.profile.type",
                    new Object[]{s}));
            throw new IllegalStateException(messageService.getMessage("unknown.profile.type",
                    new Object[]{s}));
        }

        return owner;
    }
}