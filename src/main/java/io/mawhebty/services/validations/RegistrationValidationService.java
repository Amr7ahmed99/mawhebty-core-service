package io.mawhebty.services.validations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mawhebty.enums.ParticipationTypesEnum;
import io.mawhebty.enums.UserTypeEnum;
import io.mawhebty.exceptions.*;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
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

    public void validateTalentRegistration(DraftRegistrationRequest request, List<TalentCategoryFormKeys> reqFields, TalentCategory talentCategory) {

        List<TalentCategoryFormKeys> talentCategoryFormKeys= talentCategoryFormKeysRepository.findAllByTalentCategory(talentCategory);
        List<TalentCategoryFormKeys> requeriedFields =  talentCategoryFormKeys.stream().filter(fk-> fk.getIsRequired() == true).toList();

        if(!requeriedFields.isEmpty() &&
                (request.getTalentCategoryForm() == null || request.getTalentCategoryForm().isEmpty() || request.getTalentCategoryForm().isBlank())){
            throw new BadDataException("Category form data is missing: " + requeriedFields);
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
            throw new RuntimeException(e);
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
                    throw new BadDataException("Category field is required: "+ fk.getNameEn());
                }
            }
        }

        if (request.getFile() == null) {
            throw new BadDataException("You must upload video/image file");
        }

        boolean isMediaFile = s3Service.isImageFile(request.getFile()) || s3Service.isVideoFile(request.getFile());
        if (!isMediaFile) {
            throw new BadDataException("The uploaded file must be video/image");
        }

        if (request.getParticipationTypeId() == null) {
            throw new BadDataException("Talent must have participation type");
        }
    }

    public void validateResearcherRegistration(DraftRegistrationRequest request) {

        if(request.getUserTypeId() == null){
            throw new BadDataException("User type must not be null");
        }

        boolean validUserType= false;
        for (UserTypeEnum type : UserTypeEnum.values()) {
            if (request.getUserTypeId().equals(type.getId())) {
                validUserType = true;
                break;
            }
        }

        if(!validUserType){
            throw new ResourceNotFoundException("Invalid user type with ID: "+ request.getUserTypeId());
        }

        boolean fileIsNotNull= request.getFile() != null;
        boolean isIndividualResearcher= request.getUserTypeId().equals(UserTypeEnum.INDIVIDUAL.getId());
        if(isIndividualResearcher && fileIsNotNull){
            throw new IndividualResearcherFileException();
        }

        // in case user is individual researcher, skip file handling and post creation and return
        if(isIndividualResearcher){
            return;
        }

        if(!fileIsNotNull){
            throw new CompanyDocumentRequiredException(request.getCompanyName());
        }

        boolean isMediaFile = (s3Service.isImageFile(request.getFile()) || s3Service.isVideoFile(request.getFile()));
        if (isMediaFile){
            throw new BadDataException("The uploaded file must be (doc/pdf)");
        }

    }
}
