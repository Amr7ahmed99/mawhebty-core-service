package io.mawhebty.services;

import io.mawhebty.enums.UserRoleEnum;
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
    private final IndividualResearcherProfileRepository individualResearcherProfileRepository;
    private final CompanyResearcherProfileRepository companyResearcherProfileRepository;
    private final ParticipationTypeRepository participationTypeRepository;
    private final GenderRepository genderRepository;
    private final UserTypeRepository userTypeRepository;

    public TalentProfile createTalentProfile(User user, DraftRegistrationRequest request, TalentCategory talentCategory, TalentSubCategory talentSubCategory) {
        TalentProfile profile = new TalentProfile();
        ParticipationType type = participationTypeRepository.findById(request.getParticipationTypeId())
                .orElseThrow(()-> new BadDataException("Invalid participation type with id: " + request.getParticipationTypeId()));
        profile.setUser(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
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

    public ResearcherProfile createResearcherProfile(
            User user,
            DraftRegistrationRequest request,
            TalentCategory talentCategory,
            TalentSubCategory talentSubCategory,
            boolean isIndividual
    ) {

        if (isIndividual) {
            IndividualResearcherProfile profile = new IndividualResearcherProfile();
            profile.setUser(user);
            profile.setShortBio(request.getShortBio());
            profile.setCity(request.getCity());
            profile.setCountry(request.getCountry());
            profile.setCategory(talentCategory);
            profile.setSubCategory(talentSubCategory);

            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());

            return individualResearcherProfileRepository.save(profile);

        } else {
            CompanyResearcherProfile profile = new CompanyResearcherProfile();
            profile.setUser(user);
            profile.setShortBio(request.getShortBio());
            profile.setCity(request.getCity());
            profile.setCountry(request.getCountry());
            profile.setCategory(talentCategory);
            profile.setSubCategory(talentSubCategory);
            profile.setCompanyName(request.getCompanyName());
            profile.setCommercialRegNo(request.getCommercialRegNo());
            profile.setContactPerson(request.getContactPerson());
            profile.setContactPhone(request.getContactPhone() != null? request.getContactPhone():
                    request.getPrefixCode().replace("+", "") + request.getPhone());

            return companyResearcherProfileRepository.save(profile);
        }
    }


    public ResearcherProfile getResearcherProfileByType(Long userId, UserType userType){
        if (userType == null || userType.getType() == null) {
            throw new BadDataException("user type is required");
        }
        return switch (userType.getType()) {
            case INDIVIDUAL -> individualResearcherProfileRepository.findByUserId(userId).orElse(null);
            case COMPANY -> companyResearcherProfileRepository.findByUserId(userId).orElse(null);
        };
    }

    public Object getUserProfile(User user){
        if (user.getRole().getName().equals(UserRoleEnum.TALENT)) {
            return talentProfileRepository.findByUserId(user.getId())
                    .orElseThrow(()-> new BadDataException("the user does not have talent profile"));
        }

        return this.getResearcherProfileByType(user.getId(), user.getUserType());
    }
}
