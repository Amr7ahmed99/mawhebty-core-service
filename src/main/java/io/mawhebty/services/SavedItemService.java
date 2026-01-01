package io.mawhebty.services;

import io.mawhebty.enums.SavedItemTypeEnum;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedItemService {

    private final SavedItemRepository savedItemRepository;
    private final PostRepository postRepository;
    private final EventRepository eventRepository;
    private final ArticleRepository articleRepository;
    private final MessageService messageService;
    private final UserProfileService userProfileService;
    private final SavedItemTypeRepository savedItemTypeRepository;


    @Transactional(readOnly = true)
    public Map<String, Object> getUserSavedItemsByType(Long userId, int itemType, int perPage, int offset) {
        Map<String, Object> result = new HashMap<>();

        SavedItemTypeEnum type = SavedItemTypeEnum.fromValue(itemType);

        switch (type) {
            case POST:
                List<SavedItem> savedPosts = savedItemRepository.findSavedPostsWithOwner(userId, perPage, offset);
                result.put("posts", mapToPostResponse(savedPosts));
                break;

            case EVENT:
                List<SavedItem> savedEvents = savedItemRepository.findSavedEvents(userId, perPage, offset);
                result.put("events", mapToEventResponse(savedEvents));
                break;

            case ARTICLE:
                List<SavedItem> savedArticles = savedItemRepository.findSavedArticles(userId, perPage, offset);
                result.put("articles", mapToArticleResponse(savedArticles));
                break;

            default:
                throw new IllegalArgumentException(
                        messageService.getMessage("invalid.item.type", new Object[]{itemType})
                );
        }

        return result;
    }

    @Transactional
    public boolean saveItem(User user, SavedItemTypeEnum itemType, Long itemId, String notes, String tags) {
        // Check if already saved
        if (savedItemRepository.existsByUserAndItemTypeAndItemId(user, itemType, itemId)) {
            throw new IllegalStateException(
                    messageService.getMessage("item.already.saved")
            );
        }

        // Validate item exists based on type
        validateItemExists(itemType, itemId);

        SavedItemType type= this.savedItemTypeRepository.findById(itemType.getValue())
                .orElseThrow(()-> new ResourceNotFoundException("Item type not found"));

        // Create saved item
        SavedItem savedItem = SavedItem.builder()
                .user(user)
                .itemType(type)
                .itemId(itemId)
                .notes(notes)
                .tags(tags)
                .build();

        if(type.getId().equals(SavedItemTypeEnum.POST.getValue())){
            savedItem.setPost(this.postRepository.findById(itemId)
                    .orElseThrow(()-> new BadDataException("item saved failed, post not found")));
        }else if (type.getId().equals(SavedItemTypeEnum.EVENT.getValue())){
            savedItem.setEvent(this.eventRepository.findById(itemId)
                    .orElseThrow(()-> new BadDataException("item saved failed, event not found")));
        } else if (type.getId().equals(SavedItemTypeEnum.ARTICLE.getValue())) {
            savedItem.setEvent(this.eventRepository.findById(itemId)
                    .orElseThrow(()-> new BadDataException("item saved failed, event not found")));
        }

        savedItemRepository.save(savedItem);

        log.info(messageService.getMessage("item.saved.success",
                new Object[]{user.getId(), itemType, itemId}));

        return true;
    }

    @Transactional
    public boolean unsaveItem(User user, SavedItemTypeEnum itemType, Long itemId) {
        // Check if exists
        SavedItem savedItem = savedItemRepository.findByUserAndItemTypeAndItemId(user, itemType, itemId)
                .orElseThrow(() -> new IllegalStateException(
                        messageService.getMessage("item.not.saved")
                ));

        savedItemRepository.delete(savedItem);

        log.info(messageService.getMessage("item.unsaved.success",
                new Object[]{user.getId(), itemType, itemId}));

        return true;
    }

    private void validateItemExists(SavedItemTypeEnum itemType, Long itemId) {
        switch (itemType) {
            case POST:
                postRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                messageService.getMessage("post.not.found", new Object[]{itemId})
                        ));
                break;

            case EVENT:
                eventRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                messageService.getMessage("event.not.found", new Object[]{itemId})
                        ));
                break;

            case ARTICLE:
                articleRepository.findPublishedById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                messageService.getMessage("article.not.found", new Object[]{itemId})
                        ));
                break;
        }
    }

    private List<Map<String, Object>> mapToPostResponse(List<SavedItem> savedItems) {
        return savedItems.stream().map(savedItem -> {
            Post post = savedItem.getPost();
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", post.getId());

            postMap.put("owner", this.getOwnerInfo(post.getOwnerUser()));

            postMap.put("title", post.getTitle());
            postMap.put("description", post.getCaption());
            postMap.put("image_url", post.getMediaUrl());
            postMap.put("date", post.getCreatedAt().toString());
            postMap.put("saved_at", savedItem.getCreatedAt().toString());

            return postMap;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapToEventResponse(List<SavedItem> savedItems) {
        return savedItems.stream().map(savedItem -> {
            Event event = savedItem.getEvent();
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("title", event.getTitle());
            eventMap.put("description", event.getDescription());
            eventMap.put("location", event.getLocation());
            eventMap.put("location_coordinates", event.getLocationCoordinates());
            eventMap.put("image_url", event.getCoverImageUrl());
            eventMap.put("date", event.getEventDate().toString());
            eventMap.put("saved_at", savedItem.getCreatedAt().toString());

            return eventMap;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapToArticleResponse(List<SavedItem> savedItems) {
        Locale locale = LocaleContextHolder.getLocale();
        return savedItems.stream().map(savedItem -> {
            Article article = savedItem.getArticle();
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
            articleMap.put("saved_at", savedItem.getCreatedAt().toString());

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

    // set the appropriate item reference
    public void setItemReference(Object item, SavedItem savedItem) {
        SavedItemType type;
        if (item instanceof Post postItem) {
            type= this.savedItemTypeRepository.findById(SavedItemTypeEnum.POST.getValue())
                    .orElseThrow(()-> new ResourceNotFoundException("Item type not found"));
            savedItem.setItemType(type);
            savedItem.setItemId(postItem.getId());
            savedItem.setPost(postItem);
        } else if (item instanceof Event eventItem) {
            type= this.savedItemTypeRepository.findById(SavedItemTypeEnum.EVENT.getValue())
                    .orElseThrow(()-> new ResourceNotFoundException("Item type not found"));
            savedItem.setItemType(type);
            savedItem.setItemId(eventItem.getId());
            savedItem.setEvent(eventItem);
        } else if (item instanceof Article articleItem) {
            type= this.savedItemTypeRepository.findById(SavedItemTypeEnum.ARTICLE.getValue())
                    .orElseThrow(()-> new ResourceNotFoundException("Item type not found"));
            savedItem.setItemType(type);
            savedItem.setItemId(articleItem.getId());
            savedItem.setArticle(articleItem);
        } else {
            throw new IllegalArgumentException("Unsupported item type: " + item.getClass());
        }
    }

    @Transactional(readOnly = true)
    public Long countUserSavedItems(Long userId) {
        return savedItemRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Long countUserSavedItemsByType(Long userId, SavedItemTypeEnum itemType) {
        return savedItemRepository.countByUserIdAndItemType(userId, itemType.getValue());
    }

    @Transactional(readOnly = true)
    public boolean isItemSaved(User user, SavedItemTypeEnum itemType, Long itemId) {
        return savedItemRepository.existsByUserAndItemTypeAndItemId(user, itemType, itemId);
    }
}