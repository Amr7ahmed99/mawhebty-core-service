package io.mawhebty.controllers.internalServices;

import io.mawhebty.api.v1.mawhebty.dashboard.AbstractMawhebtyDashboardController;
import io.mawhebty.dtos.requests.InternalServices.ModerateUserRequestDto;
import io.mawhebty.services.ContentModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
//("IntegrationRoutes : Routes For Integration between schedular and this core-service")
public class ContentModerationController extends AbstractMawhebtyDashboardController {

    private final ContentModerationService service;

    @PostMapping("/content-moderation")
    public ResponseEntity<Void> contentModeration(@Valid @RequestBody ModerateUserRequestDto moderateUserRequestDto) {
        this.service.moderate(moderateUserRequestDto);
        return ResponseEntity.noContent().build();
    }
}
