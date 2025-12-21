package io.mawhebty.services;

import io.mawhebty.dtos.requests.InternalServices.CreateTalentCategoryRequest;
import io.mawhebty.dtos.requests.InternalServices.CreateTalentSubCategoryRequest;
import io.mawhebty.dtos.responses.TalentCategoryResponse;
import io.mawhebty.dtos.responses.TalentSubCategoryResponse;
import io.mawhebty.enums.ParticipationTypesEnum;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.models.ParticipationType;
import io.mawhebty.models.TalentCategory;
import io.mawhebty.models.TalentCategoryFormKeys;
import io.mawhebty.models.TalentSubCategory;
import io.mawhebty.repository.ParticipationTypeRepository;
import io.mawhebty.repository.TalentCategoryFormKeysRepository;
import io.mawhebty.repository.TalentCategoryRepository;
import io.mawhebty.repository.TalentSubCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TalentCategoryService {

    private final TalentCategoryRepository talentCategoryRepository;
    private final TalentSubCategoryRepository talentSubCategoryRepository;
    private final TalentCategoryFormKeysRepository formKeysRepository;
    private final ParticipationTypeRepository participationTypeRepository;

    /**
     * Create a new category.
     */
    @Transactional
    public void createCategory(CreateTalentCategoryRequest request) {
        // Check for duplicate name
        if (talentCategoryRepository.existsByNameArAndNameEn(request.getNameEn(), request.getNameAr())) {
            throw new BadDataException(String.format("Category with name (%s) or (%s) already exists", request.getNameEn(), request.getNameAr()));
        }

        ParticipationType type= participationTypeRepository.findById(request.getParticipationTypeId())
                .orElseThrow(()-> new BadDataException("Invalid participation type with id: " + request.getParticipationTypeId()));

        TalentCategory category = talentCategoryRepository.findByPartnerId(request.getPartnerId()).orElse(null);
        if(category != null){
            throw new BadDataException("Category with partner id " + request.getPartnerId()+ " is already exist");
        }

        category = TalentCategory.builder()
                .partnerId(request.getPartnerId())
                .nameEn(request.getNameEn())
                .nameAr(request.getNameAr())
                .participationType(type)
                .build();

        talentCategoryRepository.save(category);
    }

    /**
     * Create a new subCategory.
     */
    @Transactional
    public void createSubCategory(CreateTalentSubCategoryRequest request) {
        // Check for duplicate name
        if (talentSubCategoryRepository.existsByNameArAndNameEn(request.getNameEn(), request.getNameAr())) {
            throw new BadDataException(String.format("SubCategory with name (%s) or (%s) already exists", request.getNameEn(), request.getNameAr()));
        }

        TalentCategory parent = talentCategoryRepository.findByPartnerId(request.getPartnerCategoryId())
                .orElseThrow(() -> new BadDataException("Parent category not found with id: " + request.getPartnerCategoryId()));

        TalentSubCategory subCategory = TalentSubCategory.builder()
                .partnerId(request.getPartnerId())
                .nameEn(request.getNameEn())
                .nameAr(request.getNameAr())
                .talentCategory(parent)
                .build();

        talentSubCategoryRepository.save(subCategory);
    }

    /**
     * Creates a new form field (TalentCategoryFormKey) and attaches it to a category.
     */
    @Transactional
    public  void createFormKey(
            Integer fieldKeyId,
            String nameEn,
            String nameAr,
            String fieldType,
            boolean required,
            Integer categoryPartnerId,
            Integer subCategoryPartnerId
    ) {
        // check TalentCategory exists
        TalentCategory category = talentCategoryRepository.findByPartnerId(categoryPartnerId)
                .orElseThrow(() -> new BadDataException("Talent category not found with partner id: " + categoryPartnerId));

        boolean exists;
        TalentSubCategory subCategory = null;
        if (subCategoryPartnerId != null) {
            // check TalentSubCategory exists
            subCategory = talentSubCategoryRepository.findByPartnerId(subCategoryPartnerId)
                    .orElseThrow(() -> new BadDataException("Talent subCategory not found with partner id: " + subCategoryPartnerId));

            exists = formKeysRepository.existsByFieldKeyIdAndTalentCategoryAndTalentSubCategory(fieldKeyId, category, subCategory);
            // check if this key already exists in this category
            if (exists) {
                throw new BadDataException("Field key already exists in this subCategory (fieldKeyId=" + fieldKeyId + ")");
            }
        } else {
            exists = formKeysRepository.existsByFieldKeyIdAndTalentCategory(fieldKeyId, category);
            // check if this key already exists in this category
            if (exists) {
                throw new BadDataException("Field key already exists in this category (fieldKeyId=" + fieldKeyId + ")");
            }
        }

        TalentCategoryFormKeys formKey = TalentCategoryFormKeys.builder()
                .fieldKeyId(fieldKeyId)
                .nameEn(nameEn)
                .nameAr(nameAr)
                .fieldType(fieldType)
                .isRequired(required)
                .talentCategory(category)
                .talentSubCategory(subCategory)
                .build();

        formKeysRepository.save(formKey);
    }


    public List<TalentCategoryResponse> fetchAllCategories(){

        List<TalentCategoryResponse> categories= talentCategoryRepository.findAll()
                .stream().map(tc-> TalentCategoryResponse.builder()
                        .id(tc.getPartnerId())
                        .nameAr(tc.getNameAr())
                        .nameEn(tc.getNameEn())
                        .participationTypeId(tc.getParticipationType().getId())
                        .subCategories(
                                tc.getTalentSubCategories()
                                        .stream()
                                        .map(tsc -> TalentSubCategoryResponse.builder()
                                                .id(tsc.getPartnerId())
                                                .nameEn(tsc.getNameEn())
                                                .nameAr(tsc.getNameAr())
                                                .build())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .collect(Collectors.toList());


        return categories;
    }

}
