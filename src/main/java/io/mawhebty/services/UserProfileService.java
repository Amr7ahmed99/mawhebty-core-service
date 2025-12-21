package io.mawhebty.services;

import io.mawhebty.enums.UserTypeEnum;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
import org.springframework.stereotype.Service;

import io.mawhebty.dtos.requests.DraftRegistrationRequest;
import io.mawhebty.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final TalentProfileRepository talentProfileRepository;
    private final ResearcherProfileRepository researcherProfileRepository;
    private final ParticipationTypeRepository participationTypeRepository;
    private final GenderRepository genderRepository;
    private final UserTypeRepository userTypeRepository;

    public TalentProfile createTalentProfile(User user, DraftRegistrationRequest request, TalentCategory talentCategory, TalentSubCategory talentSubCategory) {
        TalentProfile profile = new TalentProfile();
        ParticipationType type = participationTypeRepository.findById(request.getParticipationTypeId())
                .orElseThrow(()-> new BadDataException("Invalid participation type with id: " + request.getParticipationTypeId()));
        profile.setUser(user);
        profile.setFullName(request.getFirstName() + " " + request.getLastName());
        profile.setCountry(request.getCountry());
        profile.setCity(request.getCity());
        profile.setAge(request.getAge());
        profile.setParticipationType(type);
        profile.setCategory(talentCategory);
        profile.setSubCategory(talentSubCategory);

        Gender gender = genderRepository.findById(request.getGender())
            .orElseThrow(() -> new ResourceNotFoundException("Gender not found"));
        profile.setGender(gender);
        
        profile.setShortBio(request.getShortBio());
        // profile.setProfilePicture(fileUrl);

        return talentProfileRepository.save(profile);
    }
    
    public ResearcherProfile createResearcherProfile(User user, DraftRegistrationRequest request,
                                                     TalentCategory talentCategory, TalentSubCategory talentSubCategory) {
        ResearcherProfile profile = new ResearcherProfile();
        profile.setUser(user);
        
        // Fetch user type entity
        UserType userType = userTypeRepository.findById(request.getUserTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("User type not found"));

        profile.setUserType(userType);
        profile.setShortBio(request.getShortBio());
        profile.setCity(request.getCity());
        profile.setCountry(request.getCountry());
        profile.setCategory(talentCategory);
        profile.setSubCategory(talentSubCategory);

        if (userType.getId().equals(UserTypeEnum.COMPANY.getId())) {
            profile.setCompanyName(request.getCompanyName());
            profile.setCommercialRegNo(request.getCommercialRegNo());
            profile.setContactPerson(request.getContactPerson());
            profile.setContactPhone(request.getPrefixCode()+request.getPhone());
        } else {
            profile.setContactPerson(request.getFirstName() + " " + request.getLastName());
        }
        
        // profile.setProfilePicture(fileUrl);
        return researcherProfileRepository.save(profile);
    }
}
