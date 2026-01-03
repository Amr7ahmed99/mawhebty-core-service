package io.mawhebty.controllers;

import io.mawhebty.api.v1.mawhebty.platform.AbstractMawhebtyPlatformController;
import io.mawhebty.api.v1.mawhebtyPlatform.MawhebtyPlatformUserProfileApi;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.*;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.services.UserFollowService;
import io.mawhebty.services.UserProfileService;
import io.mawhebty.services.auth.CurrentUserService;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController("MawhebtyPlatformUserProfileController")
@RequiredArgsConstructor
public class UserProfileController extends AbstractMawhebtyPlatformController
        implements MawhebtyPlatformUserProfileApi {

    private final UserProfileService userProfileService;
    private final UserFollowService userFollowService;
    private final CurrentUserService currentUserService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<UserProfileDataResponseResource> userProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            // Get current authenticated user
            User currentUser = currentUserService.getCurrentUser(authentication);

            // Get user profile based on user type
            Object profile = userProfileService.getUserProfile(currentUser);

            // Get follow counts
            Map<String, Long> followCounts = userFollowService.getFollowCounts(currentUser.getId());

            // Build response based on user type
            UserProfileDataResponseResource response = buildResponse(profile, followCounts, currentUser);

            log.info(messageService.getMessage("user.profile.retrieved.success",
                    new Object[]{currentUser.getId()}));
            return ResponseEntity.ok().body(response);

        } catch (UserNotFoundException e) {
            log.error(messageService.getMessage("user.not.found"), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(messageService.getMessage("user.profile.retrieve.error"), e.getMessage(), e);
            throw new RuntimeException(messageService.getMessage("user.profile.retrieve.error"), e);
        }
    }

    private UserProfileDataResponseResource buildResponse(Object profile,
                                                          Map<String, Long> followCounts, User user) {
        Locale locale = LocaleContextHolder.getLocale();
        UserProfileDataResponseResource response = new UserProfileDataResponseResource();

        if (profile instanceof TalentProfile talentProfile) {
            buildTalentProfileResponse(response, talentProfile, followCounts, user, locale);
        } else if (profile instanceof IndividualResearcherProfile individualResearcherProfile) {
            buildIndividualResearcherResponse(response, individualResearcherProfile, followCounts, user, locale);
        } else if (profile instanceof CompanyResearcherProfile companyResearcherProfile) {
            buildCompanyResearcherResponse(response, companyResearcherProfile, followCounts, user, locale);
        } else {
            log.warn(messageService.getMessage("unknown.profile.type",
                    new Object[]{profile != null ? profile.getClass().getName() : "null"}));
            buildBasicUserResponse(response, followCounts, user);
        }

        return response;
    }

    private void buildTalentProfileResponse(UserProfileDataResponseResource response,
                                            TalentProfile talentProfile, Map<String, Long> followCounts, User user, Locale locale) {

        // Build user profile
        UserProfileResource userProfile = buildCommonUserProfile(followCounts, user);
        userProfile.setId(talentProfile.getId().intValue());
        userProfile.setFirstName(talentProfile.getFirstName());
        userProfile.setLastName(talentProfile.getLastName());
        userProfile.setImageUrl(talentProfile.getProfilePicture());
        userProfile.setShortBio(talentProfile.getShortBio());
        userProfile.setAge(talentProfile.getAge());
        userProfile.setCountry(talentProfile.getCountry());
        userProfile.setCity(talentProfile.getCity());

        response.setUser(userProfile);

        // Build category
        if (talentProfile.getCategory() != null) {
            response.setCategory(buildCategoryResource(talentProfile.getCategory(), locale));
        }

        // Build subcategory
        if (talentProfile.getSubCategory() != null) {
            response.setSubcategory(buildSubcategoryResource(talentProfile.getSubCategory(), locale));
        }

        // Build talent type
        if (talentProfile.getParticipationType() != null) {
            response.setTalentType(buildTalentTypeResource(talentProfile.getParticipationType(), locale));
        }
    }

    private void buildIndividualResearcherResponse(UserProfileDataResponseResource response,
                                                   IndividualResearcherProfile profile, Map<String, Long> followCounts, User user, Locale locale) {

        UserProfileResource userProfile = buildCommonUserProfile(followCounts, user);
        userProfile.setId(profile.getId().intValue());
        userProfile.setFirstName(profile.getFirstName());
        userProfile.setLastName(profile.getLastName());
        userProfile.setImageUrl(profile.getProfilePicture());
        userProfile.setShortBio(profile.getShortBio());
        userProfile.setCountry(profile.getCountry());
        userProfile.setCity(profile.getCity());

        response.setUser(userProfile);

        if (profile.getCategory() != null) {
            response.setCategory(buildCategoryResource(profile.getCategory(), locale));
        }

        if (profile.getSubCategory() != null) {
            response.setSubcategory(buildSubcategoryResource(profile.getSubCategory(), locale));
        }
    }

    private void buildCompanyResearcherResponse(UserProfileDataResponseResource response,
                                                CompanyResearcherProfile profile, Map<String, Long> followCounts, User user, Locale locale) {

        UserProfileResource userProfile = buildCommonUserProfile(followCounts, user);
        userProfile.setId(profile.getId().intValue());
        userProfile.setFirstName(profile.getCompanyName());
        userProfile.setContactPerson(profile.getContactPerson());
        userProfile.setContactPhone(profile.getContactPhone());
        userProfile.setImageUrl(profile.getProfilePicture());
        userProfile.setShortBio(profile.getShortBio());
        userProfile.setCountry(profile.getCountry());
        userProfile.setCity(profile.getCity());

        response.setUser(userProfile);

        if (profile.getCategory() != null) {
            response.setCategory(buildCategoryResource(profile.getCategory(), locale));
        }

        if (profile.getSubCategory() != null) {
            response.setSubcategory(buildSubcategoryResource(profile.getSubCategory(), locale));
        }
    }

    private void buildBasicUserResponse(UserProfileDataResponseResource response,
                                        Map<String, Long> followCounts, User user) {

        UserProfileResource userProfile = buildCommonUserProfile(followCounts, user);
        userProfile.setFirstName("User");
        userProfile.setLastName("");
        response.setUser(userProfile);
    }

    private UserProfileResource buildCommonUserProfile(Map<String, Long> followCounts, User user) {
        UserProfileResource userProfile = new UserProfileResource();

        userProfile.setPhone(user.getPhoneNumber());
        userProfile.setCountryCode(user.getCountryCode());
        userProfile.setUserStatus(user.getStatus().getId().intValue());
        userProfile.setUserRole(user.getRole().getId());
        userProfile.setFollowers(followCounts.getOrDefault("followers", 0L).intValue());
        userProfile.setFollowing(followCounts.getOrDefault("following", 0L).intValue());

        return userProfile;
    }

    private CategoryResource buildCategoryResource(TalentCategory category, Locale locale) {
        CategoryResource categoryResource = new CategoryResource();
        categoryResource.setId(category.getId());
        categoryResource.setName(getLocalizedName(category.getNameEn(), category.getNameAr(), locale));
        return categoryResource;
    }

    private SubcategoryResource buildSubcategoryResource(TalentSubCategory subCategory, Locale locale) {
        SubcategoryResource subcategoryResource = new SubcategoryResource();
        subcategoryResource.setId(subCategory.getId());
        subcategoryResource.setName(getLocalizedName(subCategory.getNameEn(), subCategory.getNameAr(), locale));
        return subcategoryResource;
    }

    private TalentTypeResource buildTalentTypeResource(ParticipationType participationType, Locale locale) {
        TalentTypeResource talentTypeResource = new TalentTypeResource();
        talentTypeResource.setId(participationType.getId());
        talentTypeResource.setName(getLocalizedName(participationType.getNameEn(), participationType.getNameAr(), locale));
        return talentTypeResource;
    }

    private String getLocalizedName(String nameEn, String nameAr, Locale locale) {
        return "ar".equals(locale.getLanguage()) ? nameAr : nameEn;
    }
}