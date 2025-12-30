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
import io.mawhebty.support.MessageService;
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
    private final MessageService messageService; // Added

    @Transactional
    public void createCategory(CreateTalentCategoryRequest request) {
        if (talentCategoryRepository.existsByNameArAndNameEn(request.getNameEn(), request.getNameAr())) {
            throw new BadDataException(
                    messageService.getMessage("category.already.exists",
                            new Object[]{request.getNameEn(), request.getNameAr()})
            );
        }

        ParticipationType type= participationTypeRepository.findById(request.getParticipationTypeId())
                .orElseThrow(() -> new BadDataException(
                        messageService.getMessage("invalid.participation.type",
                                new Object[]{request.getParticipationTypeId()})
                ));

        TalentCategory category = talentCategoryRepository.findByPartnerId(request.getPartnerId()).orElse(null);
        if(category != null){
            throw new BadDataException(
                    messageService.getMessage("category.partner.exists",
                            new Object[]{request.getPartnerId()})
            );
        }

        category = TalentCategory.builder()
                .partnerId(request.getPartnerId())
                .nameEn(request.getNameEn())
                .nameAr(request.getNameAr())
                .participationType(type)
                .build();

        talentCategoryRepository.save(category);
    }

    @Transactional
    public void createSubCategory(CreateTalentSubCategoryRequest request) {
        if (talentSubCategoryRepository.existsByNameArAndNameEn(request.getNameEn(), request.getNameAr())) {
            throw new BadDataException(
                    messageService.getMessage("subcategory.already.exists",
                            new Object[]{request.getNameEn(), request.getNameAr()})
            );
        }

        TalentCategory parent = talentCategoryRepository.findByPartnerId(request.getPartnerCategoryId())
                .orElseThrow(() -> new BadDataException(
                        messageService.getMessage("parent.category.not.found",
                                new Object[]{request.getPartnerCategoryId()})
                ));

        TalentSubCategory subCategory = TalentSubCategory.builder()
                .partnerId(request.getPartnerId())
                .nameEn(request.getNameEn())
                .nameAr(request.getNameAr())
                .talentCategory(parent)
                .build();

        talentSubCategoryRepository.save(subCategory);
    }

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
        TalentCategory category = talentCategoryRepository.findByPartnerId(categoryPartnerId)
                .orElseThrow(() -> new BadDataException(
                        messageService.getMessage("talent.category.not.found.partner",
                                new Object[]{categoryPartnerId})
                ));

        boolean exists;
        TalentSubCategory subCategory = null;
        if (subCategoryPartnerId != null) {
            subCategory = talentSubCategoryRepository.findByPartnerId(subCategoryPartnerId)
                    .orElseThrow(() -> new BadDataException(
                            messageService.getMessage("talent.subcategory.not.found.partner",
                                    new Object[]{subCategoryPartnerId})
                    ));

            exists = formKeysRepository.existsByFieldKeyIdAndTalentCategoryAndTalentSubCategory(fieldKeyId, category, subCategory);
            if (exists) {
                throw new BadDataException(
                        messageService.getMessage("field.key.exists.subcategory",
                                new Object[]{fieldKeyId})
                );
            }
        } else {
            exists = formKeysRepository.existsByFieldKeyIdAndTalentCategory(fieldKeyId, category);
            if (exists) {
                throw new BadDataException(
                        messageService.getMessage("field.key.exists.category",
                                new Object[]{fieldKeyId})
                );
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