package io.mawhebty.services;

import io.mawhebty.api.v1.resources.mawhebtyPlatform.EventSummaryResource;
import io.mawhebty.dtos.requests.CreateEventRequest;
import io.mawhebty.dtos.requests.UpdateEventRequest;
import io.mawhebty.dtos.responses.*;
import io.mawhebty.enums.EventStatus;
import io.mawhebty.enums.EventType;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.repository.EventRepository;
import io.mawhebty.repository.EventAttendeeRepository;
import io.mawhebty.repository.TalentCategoryRepository;
import io.mawhebty.repository.TalentSubCategoryRepository;
import io.mawhebty.repository.specification.EventSpecification;
import io.mawhebty.services.auth.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final CurrentUserService currentUserService;
    private final TalentCategoryRepository talentCategoryRepository;
    private final TalentSubCategoryRepository talentSubCategoryRepository;


    // Get events with filtering and pagination
    public EventListResponse getEvents(
            String status,
            EventType type,
            Boolean isFree,
            String search,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer page,
            Integer perPage,
            String sortBy
    ) {

        Sort sort = Sort.by("eventDate").descending();
        if (sortBy != null) {
            sort = sortBy.equals("date_asc")
                    ? Sort.by("eventDate").ascending()
                    : Sort.by("eventDate").descending();
        }

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);

        Specification<Event> spec = Specification.allOf(
                EventSpecification.hasStatus(
                        status != null ? EventStatus.valueOf(status.toUpperCase()) : null),
                EventSpecification.hasType(type),
                EventSpecification.isFree(isFree),
                EventSpecification.search(search),
                EventSpecification.betweenDates(startDate, endDate)
        );

        Page<Event> eventPage = eventRepository.findAll(spec, pageable);

        return EventListResponse.builder()
                .total(eventPage.getTotalElements())
                .page(page)
                .perPage(perPage)
                .totalPages(eventPage.getTotalPages())
                .events(
                        eventPage.getContent()
                                .stream()
                                .map(this::mapToEventListItemResponse)
                                .toList()
                )
                .build();
    }


    // Get event by ID
    public EventWithRelatedResponse getEventById(Long id) {
        // Get the main event
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // Get 5 related events based on category and subCategory, excluding the main event
        List<Event> relatedEvents = eventRepository.findTop5ByCategoryAndIdNotOrderByEventDateAsc(
                event.getCategory(),event.getId());

        // Map main event
        EventWithRelatedResponse response = mapToEventWithRelatedResponse(event);

        // Map related events
        List<EventSummaryResource> related = relatedEvents.stream()
                .map(this::mapToEventSummary)
                .toList();

        response.setRelatedEvents(related);

        return response;
    }

    // Mapper for EventSummary
    private EventSummaryResource mapToEventSummary(Event event) {
        Locale locale = LocaleContextHolder.getLocale();
        EventSummaryResource summary = new EventSummaryResource();
        summary.setId(BigDecimal.valueOf(event.getId()));
        summary.setTitle("en".equals(locale.getLanguage()) ? event.getTitleEn() : event.getTitleAr());
        summary.setDescription("en".equals(locale.getLanguage()) ? event.getDescriptionEn() : event.getDescriptionAr());
        summary.setLocation(event.getLocation());
        summary.setImageUrl(event.getCoverImageUrl());
        summary.setDate(event.getEventDate());
        return summary;
    }

    // Create event
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        // Validate event date
        if (request.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadDataException("Event date must be in the future");
        }

        // Validate end date
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getEventDate())) {
            throw new BadDataException("End date must be after event date");
        }

        TalentCategory category= talentCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new ResourceNotFoundException("Event category not found"));

        TalentSubCategory subCategory= null;
        // Validate subCategory
        if (request.getSubCategoryId() != null){
            subCategory= talentSubCategoryRepository.findById(request.getSubCategoryId())
                    .orElseThrow( ()-> new ResourceNotFoundException("Event subCategory not found"));
        }

        // Create event entity
        Event event = Event.builder()
                .titleEn(request.getTitleEn())
                .titleAr(request.getTitleAr())
                .descriptionEn(request.getDescriptionEn())
                .descriptionAr(request.getDescriptionAr())
                .eventDate(request.getEventDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .locationCoordinates(request.getLocationCoordinates())
                .coverImageUrl(request.getCoverImageUrl())
                .status(request.getStatus())
                .type(request.getType())
                .maxAttendees(request.getMaxAttendees())
                .isFree(request.getIsFree())
                .ticketPrice(request.getTicketPrice())
                .registrationUrl(request.getRegistrationUrl())
                .eventLink(request.getEventLink())
                .tags(request.getTags())
                .currentAttendees(0)
                .category(category)
                .subCategory(subCategory)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Event created with id: {}", savedEvent.getId());

        return mapToEventResponse(savedEvent);
    }

    // Update event
    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // Update fields if provided
        if (request.getTitleEn() != null) {
            event.setTitleEn(request.getTitleEn());
        }
        if (request.getTitleAr() != null) {
            event.setTitleAr(request.getTitleAr());
        }
        if (request.getDescriptionEn() != null) {
            event.setDescriptionEn(request.getDescriptionEn());
        }
        if (request.getDescriptionAr() != null) {
            event.setDescriptionAr(request.getDescriptionAr());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now())) {
                throw new BadDataException("Event date must be in the future");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(event.getEventDate())) {
                throw new BadDataException("End date must be after event date");
            }
            event.setEndDate(request.getEndDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getLocationCoordinates() != null) {
            event.setLocationCoordinates(request.getLocationCoordinates());
        }
        if (request.getCoverImageUrl() != null) {
            event.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getStatus() != null) {
            event.setStatus(EventStatus.UPCOMING);
            event.setStatus(request.getStatus());
        }
        if (request.getType() != null) {
            event.setType(request.getType());
//            event.setType(EventType.GENERAL);
        }
        if (request.getMaxAttendees() != null) {
            event.setMaxAttendees(request.getMaxAttendees());
        }
        if (request.getIsFree() != null) {
            event.setIsFree(request.getIsFree());
        }
        if (request.getTicketPrice() != null) {
            event.setTicketPrice(request.getTicketPrice());
        }
        if (request.getRegistrationUrl() != null) {
            event.setRegistrationUrl(request.getRegistrationUrl());
        }
        if (request.getEventLink() != null) {
            event.setEventLink(request.getEventLink());
        }
        if (request.getTags() != null) {
            event.setTags(request.getTags());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated with id: {}", id);

        return mapToEventResponse(updatedEvent);
    }

    // Delete event
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }

        eventRepository.deleteById(id);
        log.info("Event deleted with id: {}", id);
    }

    // Register for event
    @Transactional
    public EventRegistrationResponse registerForEvent(Long eventId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = currentUserService.getCurrentUser(authentication);

        // Get event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Check if event is cancelled
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BadDataException("Cannot register for cancelled event");
        }

        // Check if event is full
        if (event.getMaxAttendees() != null &&
                event.getCurrentAttendees() >= event.getMaxAttendees()) {
            throw new BadDataException("Event is full");
        }

        // Check if user already registered
        boolean alreadyRegistered = eventAttendeeRepository.existsByEventIdAndUserId(eventId, currentUser.getId());
        if (alreadyRegistered) {
            throw new BadDataException("User already registered for this event");
        }

        // Create registration
        EventAttendee registration = EventAttendee.builder()
                .event(event)
                .user(currentUser)
                .registeredAt(LocalDateTime.now())
                .build();

        EventAttendee savedRegistration = eventAttendeeRepository.save(registration);

        // Update attendee count
        event.setCurrentAttendees(event.getCurrentAttendees() + 1);
        eventRepository.save(event);

        log.info("User {} registered for event {}", currentUser.getId(), eventId);

        return EventRegistrationResponse.builder()
                .success(true)
                .message("Successfully registered for event")
                .registrationId(savedRegistration.getId())
                .eventId(eventId)
                .userId(currentUser.getId())
                .registeredAt(savedRegistration.getRegisteredAt())
                .build();
    }

    // Helper method to map Event to EventResponse
    private EventResponse mapToEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .titleEn(event.getTitleEn())
                .titleAr(event.getTitleAr())
                .descriptionEn(event.getDescriptionEn())
                .descriptionAr(event.getDescriptionAr())
                .eventDate(event.getEventDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .locationCoordinates(event.getLocationCoordinates())
                .coverImageUrl(event.getCoverImageUrl())
                .status(event.getStatus())
                .type(event.getType())
                .maxAttendees(event.getMaxAttendees())
                .currentAttendees(event.getCurrentAttendees())
                .isFree(event.getIsFree())
                .ticketPrice(event.getTicketPrice())
                .registrationUrl(event.getRegistrationUrl())
                .eventLink(event.getEventLink())
                .tags(event.getTags())
                .categoryId(event.getCategory().getId())
                .subCategoryId(event.getSubCategory() != null? event.getSubCategory().getId(): null)
                .build();
    }

    private EventListItemResponse mapToEventListItemResponse(Event event) {
        Locale locale = LocaleContextHolder.getLocale();
        return EventListItemResponse.builder()
                .id(event.getId())
                .title("en".equals(locale.getLanguage()) ? event.getTitleEn() : event.getTitleAr())
                .description("en".equals(locale.getLanguage()) ? event.getDescriptionEn() : event.getDescriptionAr())
                .date(event.getEventDate())
                .location(event.getLocation())
                .coverImageUrl(event.getCoverImageUrl())
                .build();
    }

    private EventWithRelatedResponse mapToEventWithRelatedResponse(Event event) {
        Locale locale = LocaleContextHolder.getLocale();
        return EventWithRelatedResponse.builder()
                .id(event.getId())
                .title("en".equals(locale.getLanguage()) ? event.getTitleEn() : event.getTitleAr())
                .description("en".equals(locale.getLanguage()) ? event.getDescriptionEn() : event.getDescriptionAr())
                .date(event.getEventDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .locationCoordinates(event.getLocationCoordinates())
                .imageUrl(event.getCoverImageUrl())
                .status(event.getStatus().name())
                .type(event.getType().name())
                .maxAttendees(event.getMaxAttendees())
                .currentAttendees(event.getCurrentAttendees())
                .isFree(event.getIsFree())
                .ticketPrice(event.getTicketPrice())
                .registrationUrl(event.getRegistrationUrl())
                .eventLink(event.getEventLink())
                .tags(event.getTags())
                .build();
    }

}