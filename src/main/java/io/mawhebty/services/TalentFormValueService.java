package io.mawhebty.services;

import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.models.TalentCategoryFormKeys;
import io.mawhebty.models.TalentCategoryFormValue;
import io.mawhebty.models.TalentProfile;
import io.mawhebty.repository.TalentCategoryFormKeysRepository;
import io.mawhebty.repository.TalentCategoryFormValueRepository;
import io.mawhebty.repository.TalentProfileRepository;
import io.mawhebty.support.MessageService;
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
    private final MessageService messageService; // Added

    @Transactional
    public void saveTalentFormValues(TalentProfile talentProfile, Map<Integer, Object> formFieldsValuesMap,
                                     List<TalentCategoryFormKeys> talentCategoryFormKeys) {

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
    }

    @Transactional
    public void updateTalentFormValue(Long talentProfileId, Integer fieldKeyId, Object rawValue) {
        TalentProfile talentProfile = talentProfileRepository.findById(talentProfileId)
                .orElseThrow(() -> new BadDataException(
                        messageService.getMessage("talent.profile.not.found.id",
                                new Object[]{talentProfileId})
                ));

        TalentCategoryFormKeys formKey = formKeysRepository.findByFieldKeyId(fieldKeyId)
                .orElseThrow(() -> new BadDataException(
                        messageService.getMessage("form.key.not.found",
                                new Object[]{fieldKeyId})
                ));

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