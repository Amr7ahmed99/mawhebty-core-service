package io.mawhebty.services;

import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.models.TalentCategoryFormKeys;
import io.mawhebty.models.TalentCategoryFormValue;
import io.mawhebty.models.TalentProfile;
import io.mawhebty.repository.TalentCategoryFormKeysRepository;
import io.mawhebty.repository.TalentCategoryFormValueRepository;
import io.mawhebty.repository.TalentProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TalentFormValueService {

    private final TalentProfileRepository talentProfileRepository;
    private final TalentCategoryFormKeysRepository formKeysRepository;
    private final TalentCategoryFormValueRepository formValueRepository;

    @Transactional
    public void saveTalentFormValues(TalentProfile talentProfile, Map<Integer, Object> formFieldsValuesMap,
                                     List<TalentCategoryFormKeys> talentCategoryFormKeys) {

        // prepare list of formValues
        List<TalentCategoryFormValue> talentCategoryFormValues= new ArrayList<>();

        talentCategoryFormKeys.forEach(formKey->{
            Object rawValue= formFieldsValuesMap.get(formKey.getFieldKeyId());
            String storedValue;
            if (rawValue instanceof List<?>) {
                storedValue = ((List<?>) rawValue).stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
            } else {
                storedValue = String.valueOf(rawValue);
            }
            talentCategoryFormValues.add(TalentCategoryFormValue.builder()
                    .talentProfile(talentProfile)
                    .formKey(formKey)
                    .value(storedValue)
                    .build());
        });

        formValueRepository.saveAll(talentCategoryFormValues);

//        for (Map.Entry<Integer, Object> entry : formFieldsValuesMap.entrySet()) {
//            Integer fieldKeyId = entry.getKey();
//            Object rawValue = entry.getValue();
//
//            TalentCategoryFormKeys formKey = formKeysRepository.findByFieldKeyId(fieldKeyId)
//                    .orElseThrow(() -> new RuntimeException("Form key not found: " + fieldKeyId));
//
//            String storedValue;
//            if (rawValue instanceof List<?>) {
//                storedValue = ((List<?>) rawValue).stream()
//                        .map(String::valueOf)
//                        .collect(Collectors.joining(","));
//            } else {
//                storedValue = String.valueOf(rawValue);
//            }
//
//            talentCategoryFormValues.add(TalentCategoryFormValue.builder()
//                    .talentProfile(talentProfile)
//                    .formKey(formKey)
//                    .value(storedValue)
//                    .build());
//        }
//
//        formValueRepository.saveAll(talentCategoryFormValues);
    }

    @Transactional
    public void updateTalentFormValue(Long talentProfileId, Integer fieldKeyId, Object rawValue) {
        TalentProfile talentProfile = talentProfileRepository.findById(talentProfileId)
                .orElseThrow(() -> new BadDataException("Talent profile not found"));

        TalentCategoryFormKeys formKey = formKeysRepository.findByFieldKeyId(fieldKeyId)
                .orElseThrow(() -> new BadDataException("Form key not found: " + fieldKeyId));

        String storedValue;
        if (rawValue instanceof List<?>) {
            storedValue = ((List<?>) rawValue).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else {
            storedValue = String.valueOf(rawValue);
        }

        TalentCategoryFormValue formValue = formValueRepository
                .findByTalentProfileAndFormKey(talentProfile, formKey)
                .orElse(TalentCategoryFormValue.builder()
                        .talentProfile(talentProfile)
                        .formKey(formKey)
                        .build());

        formValue.setValue(storedValue);
        formValueRepository.save(formValue);
    }

}

