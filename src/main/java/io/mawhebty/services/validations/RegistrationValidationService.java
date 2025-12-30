package io.mawhebty.services.validations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mawhebty.enums.ParticipationTypesEnum;
import io.mawhebty.enums.UserTypeEnum;
import io.mawhebty.exceptions.*;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
import io.mawhebty.support.MessageService;
import org.springframework.stereotype.Service;

import io.mawhebty.dtos.requests.DraftRegistrationRequest;
import io.mawhebty.services.S3Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationValidationService {

    private final S3Service s3Service;
    private final TalentCategoryFormKeysRepository talentCategoryFormKeysRepository;
    private final MessageService messageService; // Added

    public void validateTalentRegistration(DraftRegistrationRequest request, List<TalentCategoryFormKeys> reqFields, TalentCategory talentCategory) {

        List<TalentCategoryFormKeys> talentCategoryFormKeys= talentCategoryFormKeysRepository.findAllByTalentCategory(talentCategory);
        List<TalentCategoryFormKeys> requeriedFields =  talentCategoryFormKeys.stream().filter(fk-> fk.getIsRequired() == true).toList();

        if(!requeriedFields.isEmpty() &&
                (request.getTalentCategoryForm() == null || request.getTalentCategoryForm().isEmpty() || request.getTalentCategoryForm().isBlank())){
            throw new BadDataException(
                    messageService.getMessage("category.form.data.missing",
                            new Object[]{requeriedFields})
            );
        }

        // extract keys and values from talentCategoryForm
        ObjectMapper mapper = new ObjectMapper();
        Map<Integer, Object> talentCategoryFormMap;
        try {
            talentCategoryFormMap = mapper.readValue(
                    request.getTalentCategoryForm(),
                    new TypeReference<Map<Integer, Object>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    messageService.getMessage("category.form.parse.error")
            );
        }

        request.setTalentCategoryFormMap(talentCategoryFormMap);

        List<Integer> fieldsIds= request.getTalentCategoryFormMap().keySet().stream().toList();

        if(!talentCategoryFormKeys.isEmpty()){
            for(TalentCategoryFormKeys fk: talentCategoryFormKeys){
                if(fieldsIds.contains(fk.getFieldKeyId())){
                    reqFields.add(fk);
                    continue;
                }
                if(fk.getIsRequired()){
                    throw new BadDataException(
                            messageService.getMessage("category.field.required",
                                    new Object[]{fk.getNameEn()})
                    );
                }
            }
        }

        if (request.getFile() == null) {
            throw new BadDataException(
                    messageService.getMessage("file.upload.required")
            );
        }

        boolean isMediaOrDocFile = s3Service.isImageFile(request.getFile()) ||
                s3Service.isVideoFile(request.getFile()) || s3Service.isDocumentFile(request.getFile());
        if (!isMediaOrDocFile) {
            throw new BadDataException(
                    messageService.getMessage("invalid.file.format")
            );
        }

        if (request.getParticipationTypeId() == null) {
            throw new BadDataException(
                    messageService.getMessage("participation.type.required")
            );
        }
    }

    public void validateResearcherRegistration(DraftRegistrationRequest request, boolean isIndividualResearcher) {

        boolean fileIsNotNull= request.getFile() != null;
        if(isIndividualResearcher && fileIsNotNull){
            throw new IndividualResearcherFileException(
                    messageService.getMessage("individual.researcher.file.not.allowed")
            );
        }

        // in case user is individual researcher, skip file handling and post creation and return
        if(isIndividualResearcher){
            return;
        }

        if(request.getCompanyName()==null || request.getCompanyName().isBlank()){
            throw new BadDataException(
                    messageService.getMessage("company.name.required")
            );
        }
        if(request.getContactPerson() == null || request.getContactPerson().isBlank()){
            throw new BadDataException(
                    messageService.getMessage("contact.person.required")
            );
        }
        if(request.getCommercialRegNo() == null || request.getCommercialRegNo().isBlank()){
            throw new BadDataException(
                    messageService.getMessage("commercial.reg.no.required")
            );
        }

        if(!fileIsNotNull){
            throw new CompanyDocumentRequiredException(
                    messageService.getMessage("company.document.required",
                            new Object[]{request.getCompanyName()})
            );
        }

        boolean isMediaFile = (s3Service.isImageFile(request.getFile()) || s3Service.isVideoFile(request.getFile()));
        if (isMediaFile){
            throw new BadDataException(
                    messageService.getMessage("company.document.invalid.format")
            );
        }
    }
}