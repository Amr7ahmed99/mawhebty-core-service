package io.mawhebty.controllers;

import io.mawhebty.api.v1.mawhebty.platform.AbstractMawhebtyPlatformController;
import io.mawhebty.api.v1.mawhebtyPlatform.EventsApi;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.*;
import io.mawhebty.dtos.responses.*;
import io.mawhebty.enums.EventType;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.models.User;
import io.mawhebty.services.EventService;
import io.mawhebty.services.auth.CurrentUserService;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController("EventsPlatformController")
@RequiredArgsConstructor
public class EventsController extends AbstractMawhebtyPlatformController
        implements EventsApi {

    private final EventService eventService;
    private final CurrentUserService currentUserService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<EventListResponseResource> getEvents(
            String status,
            String type,
            Boolean isFree,
            String search,
            Integer page,
            Integer perPage,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            String sortBy
           ) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = currentUserService.getCurrentUser(authentication);

            // Validate pagination
            if (page == null || page < 1) page = 1;
            if (perPage == null || perPage < 1) perPage = 10;
            if (perPage > 50) perPage = 50;

            // Get events with filtering
            EventListResponse response = eventService.getEvents(
                    status,
                    type != null ? EventType.valueOf(type) : null,
                    isFree,
                    search,
                    startDate,
                    endDate,
                    page,
                    perPage,
                    sortBy
            );

            // Convert to API resource
            EventListResponseResource apiResponse = mapToEventListResponseResource(response);

            log.info("Events retrieved successfully for user: {}", currentUser.getId());
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Error retrieving events", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<EventWithRelatedResponseResource> getEventById(BigDecimal id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = currentUserService.getCurrentUser(authentication);

            // Get event with related events
            EventWithRelatedResponse response = eventService.getEventById(id.longValue());

            // Convert to API resource
            EventWithRelatedResponseResource apiResponse = mapToEventWithRelatedResponseResource(response);

            log.info("Event retrieved successfully for user: {}", currentUser.getId());
            return ResponseEntity.ok(apiResponse);

        } catch (ResourceNotFoundException e) {
            log.error("Event not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving event", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<EventRegistrationResponseResource> registerForEvent(Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = currentUserService.getCurrentUser(authentication);

            // Register for event
            EventRegistrationResponse response = eventService.registerForEvent(id.longValue());

            // Convert to API resource
            EventRegistrationResponseResource apiResponse = mapToEventRegistrationResponseResource(response);

            log.info("User {} registered for event {}", currentUser.getId(), id);
            return ResponseEntity.ok(apiResponse);

        } catch (ResourceNotFoundException e) {
            log.error("Event not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error registering for event", e);
            throw e;
        }
    }

    // Mapper methods
    private EventListItemResource mapToEventListItemResponseResource(EventListItemResponse response) {
        EventListItemResource resource = new EventListItemResource();
        resource.setId(response.getId().intValue());
        resource.setTitle(response.getTitle());
        resource.setDescription(response.getDescription());
        resource.setDate(response.getDate());
        resource.setLocation(response.getLocation());
        resource.setImageUrl(response.getCoverImageUrl());
        return resource;
    }

    private EventListResponseResource mapToEventListResponseResource(EventListResponse response) {
        EventListResponseResource resource = new EventListResponseResource();
        resource.setTotalItems(BigDecimal.valueOf(response.getTotal()));
        resource.setCurrentPage(response.getPage());
        resource.setPerPage(response.getPerPage());
        resource.setTotalPages(response.getTotalPages());

        // Map events list
        List<EventListItemResource> eventResources = response.getEvents().stream()
                .map(this::mapToEventListItemResponseResource)
                .collect(Collectors.toList());
        resource.setEvents(eventResources);

        return resource;
    }

    private EventRegistrationResponseResource mapToEventRegistrationResponseResource(
            EventRegistrationResponse response) {
        EventRegistrationResponseResource resource = new EventRegistrationResponseResource();
        resource.setSuccess(response.getSuccess());
        resource.setMessage(response.getMessage());
        resource.setRegistrationId(response.getRegistrationId().intValue());
        resource.setEventId(response.getEventId().intValue());
        resource.setUserId(response.getUserId().intValue());
        resource.setRegisteredAt(response.getRegisteredAt());
        return resource;
    }

    private EventWithRelatedResponseResource mapToEventWithRelatedResponseResource(EventWithRelatedResponse response) {
        if(response == null){
            return new EventWithRelatedResponseResource();
        }
        EventWithRelatedResponseResource resource = new EventWithRelatedResponseResource();
        resource.setId(BigDecimal.valueOf(response.getId()));
        resource.setTitle(response.getTitle());
        resource.setDescription(response.getDescription());
        resource.setDate(response.getDate());
        resource.setEndDate(response.getEndDate());
        resource.setLocation(response.getLocation());
        resource.setLocationCoordinates(response.getLocationCoordinates());
        resource.setImageUrl(response.getImageUrl());
        resource.setStatus(response.getStatus());
        resource.setType(response.getType());
        resource.setMaxAttendees(response.getMaxAttendees());
        resource.setCurrentAttendees(response.getCurrentAttendees());
        resource.setIsFree(response.getIsFree());
        resource.setTicketPrice(response.getTicketPrice()!= null? BigDecimal.valueOf(response.getTicketPrice()): null);
        resource.setRegistrationUrl(response.getRegistrationUrl());
        resource.setEventLink(response.getEventLink());
        resource.setTags(response.getTags());
        resource.setRelatedEvents(response.getRelatedEvents());
        return resource;
    }
}