package io.mawhebty.controllers;

import io.mawhebty.dtos.requests.TalentSpecialCaseRequest;
import io.mawhebty.services.TalentSpecialCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/talent")
@RequiredArgsConstructor
public class TalentController {

    private final TalentSpecialCaseService service;

    @PostMapping(value = "/special-case", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSpecialCase(
            @Valid @ModelAttribute TalentSpecialCaseRequest request
    ) {
        service.createSpecialCase(request);
        return ResponseEntity.noContent().build();
    }
}
