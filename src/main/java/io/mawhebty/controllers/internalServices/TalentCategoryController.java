package io.mawhebty.controllers.internalServices;

import io.mawhebty.dtos.requests.InternalServices.CreateFormKeyRequest;
import io.mawhebty.dtos.requests.InternalServices.CreateTalentCategoryRequest;
import io.mawhebty.dtos.requests.InternalServices.CreateTalentSubCategoryRequest;
import io.mawhebty.services.TalentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/internal-services/core")
public class TalentCategoryController {

    private final TalentCategoryService talentCategoryService;

    @PostMapping("/categories")
    public ResponseEntity<Void> createCategory(@Valid @RequestBody CreateTalentCategoryRequest request) {
        talentCategoryService.createCategory(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sub-categories")
    public ResponseEntity<Void> createSubCategory(@Valid @RequestBody CreateTalentSubCategoryRequest request) {
        talentCategoryService.createSubCategory(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories/form-keys")
    public ResponseEntity<Void> createFormKey(
            @RequestBody CreateFormKeyRequest request
    ) {
        talentCategoryService.createFormKey(
                request.getFieldKeyId(),
                request.getNameEn(),
                request.getNameAr(),
                request.getFieldType(),
                request.isRequired(),
                request.getCategoryPartnerId(),
                request.getSubCategoryPartnerId()
        );
        return ResponseEntity.noContent().build();
    }
}
