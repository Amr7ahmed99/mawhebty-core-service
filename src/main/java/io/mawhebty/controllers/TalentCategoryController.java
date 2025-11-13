package io.mawhebty.controllers;

import io.mawhebty.dtos.responses.TalentCategoryResponse;
import io.mawhebty.services.TalentCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController("TalentCategoryControllerForPlatform")
@RequiredArgsConstructor
@RequestMapping("api/v1/categories")
public class TalentCategoryController {

    private final TalentCategoryService talentCategoryService;

//    @PostMapping
//    public ResponseEntity<Void> createCategory(@Valid @RequestBody CreateTalentCategoryRequest request) {
//        talentCategoryService.createCategory(request);
//        return ResponseEntity.noContent().build();
//    }

    @GetMapping
    public ResponseEntity<List<TalentCategoryResponse>> getAllCategories(){
        return ResponseEntity.ok().body(this.talentCategoryService.fetchAllCategories());
    }


}
