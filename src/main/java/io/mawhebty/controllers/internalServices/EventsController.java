package io.mawhebty.controllers.internalServices;

import io.mawhebty.api.v1.mawhebty.dashboard.AbstractMawhebtyDashboardController;
import io.mawhebty.api.v1.mawhebtyDashboard.EventsApi;
import io.mawhebty.api.v1.resources.mawhebtyDashboard.CreateEventRequestResource;
import io.mawhebty.api.v1.resources.mawhebtyDashboard.EventResponseResource;
import io.mawhebty.api.v1.resources.mawhebtyDashboard.UpdateEventRequestResource;
import io.mawhebty.dtos.requests.CreateEventRequest;
import io.mawhebty.dtos.requests.UpdateEventRequest;
import io.mawhebty.dtos.responses.*;
import io.mawhebty.enums.EventStatus;
import io.mawhebty.enums.EventType;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.services.EventService;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@Slf4j
@RestController("EventsDashboardController")
@RequiredArgsConstructor
public class EventsController extends AbstractMawhebtyDashboardController
        implements EventsApi {

    private final EventService eventService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<EventResponseResource> createEvent(CreateEventRequestResource createEventRequestResource) {
        try {
            // Map request to DTO
            CreateEventRequest request = mapToCreateEventRequest(createEventRequestResource);

            // Create event
            EventResponse response = eventService.createEvent(request);

            // Convert to API resource
            EventResponseResource apiResponse = mapToEventResponseResource(response);

            log.info("Event created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (Exception e) {
            log.error("Error creating event", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<EventResponseResource> updateEvent(BigDecimal id, UpdateEventRequestResource updateEventRequestResource) {
        try {
            // Map request to DTO
            UpdateEventRequest request = mapToUpdateEventRequest(updateEventRequestResource);

            // Update event
            EventResponse response = eventService.updateEvent(id.longValue(), request);

            // Convert to API resource
            EventResponseResource apiResponse = mapToEventResponseResource(response);

            log.info("Event updated successfully");
            return ResponseEntity.ok(apiResponse);

        } catch (ResourceNotFoundException e) {
            log.error("Event not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error updating event", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> deleteEvent(BigDecimal id) {
        try {
            // Delete event
            eventService.deleteEvent(id.longValue());

            log.info("Event deleted successfully");
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            log.error("Event not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting event", e);
            throw e;
        }
    }

    // Mapper methods
    private EventResponseResource mapToEventResponseResource(EventResponse response) {
        EventResponseResource resource = new EventResponseResource();
        resource.setId(response.getId().intValue());
        resource.setTitleEn(response.getTitleEn());
        resource.setTitleAr(response.getTitleAr());
        resource.setDescriptionEn(response.getDescriptionEn());
        resource.setDescriptionAr(response.getDescriptionAr());
        resource.setEventDate(response.getEventDate());
        resource.setEndDate(response.getEndDate());
        resource.setLocation(response.getLocation());
        resource.setLocationCoordinates(response.getLocationCoordinates());
        resource.setCoverImageUrl(response.getCoverImageUrl());
        resource.setStatus(response.getStatus().name());
        resource.setType(response.getType().name());
        resource.setMaxAttendees(response.getMaxAttendees());
        resource.setCurrentAttendees(response.getCurrentAttendees());
        resource.setIsFree(response.getIsFree());
        resource.setTicketPrice(BigDecimal.valueOf(response.getTicketPrice()));
        resource.setRegistrationUrl(response.getRegistrationUrl());
        resource.setEventLink(response.getEventLink());
        resource.setTags(response.getTags());
        resource.setCategoryId(BigDecimal.valueOf(response.getCategoryId()));
        resource.setSubCategoryId(response.getSubCategoryId() != null?
                BigDecimal.valueOf(response.getSubCategoryId()): null);
        return resource;
    }

    private CreateEventRequest mapToCreateEventRequest(CreateEventRequestResource resource) {
        return CreateEventRequest.builder()
                .titleEn(resource.getTitleEn())
                .titleAr(resource.getTitleAr())
                .descriptionEn(resource.getDescriptionEn())
                .descriptionAr(resource.getDescriptionAr())
                .eventDate(resource.getEventDate())
                .endDate(resource.getEndDate())
                .location(resource.getLocation())
                .locationCoordinates(resource.getLocationCoordinates())
                .coverImageUrl(resource.getCoverImageUrl())
                .status(resource.getStatus() != null ? EventStatus.valueOf(resource.getStatus().getValue()) : EventStatus.UPCOMING)
                .type(resource.getType() != null ? EventType.valueOf(resource.getType().getValue()) : EventType.GENERAL)
                .maxAttendees(resource.getMaxAttendees())
                .isFree(resource.getIsFree() != null ? resource.getIsFree() : true)
                .ticketPrice(resource.getTicketPrice().doubleValue())
                .registrationUrl(resource.getRegistrationUrl())
                .eventLink(resource.getEventLink())
                .tags(resource.getTags())
                .categoryId(resource.getCategoryId().intValue())
                .subCategoryId(resource.getSubCategoryId() != null ?
                        resource.getSubCategoryId().intValue() : null)
                .build();
    }

    private UpdateEventRequest mapToUpdateEventRequest(UpdateEventRequestResource resource) {
        return UpdateEventRequest.builder()
                .titleEn(resource.getTitleEn())
                .titleAr(resource.getTitleAr())
                .descriptionEn(resource.getDescriptionEn())
                .descriptionAr(resource.getDescriptionAr())
                .eventDate(resource.getEventDate())
                .endDate(resource.getEndDate())
                .location(resource.getLocation())
                .locationCoordinates(resource.getLocationCoordinates())
                .coverImageUrl(resource.getCoverImageUrl())
                .status(resource.getStatus() != null ? EventStatus.valueOf(resource.getStatus().name()) : null)
                .type(resource.getType() != null ? EventType.valueOf(resource.getType().name()) : null)
                .maxAttendees(resource.getMaxAttendees())
                .isFree(resource.getIsFree())
                .ticketPrice(resource.getTicketPrice().doubleValue())
                .registrationUrl(resource.getRegistrationUrl())
                .eventLink(resource.getEventLink())
                .tags(resource.getTags())
                .build();
    }
}