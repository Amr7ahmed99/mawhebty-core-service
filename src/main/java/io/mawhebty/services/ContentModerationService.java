package io.mawhebty.services;

import io.mawhebty.dtos.requests.InternalServices.ModerateUserRequestDto;
import io.mawhebty.enums.ModerationTypeEnum;
import io.mawhebty.exceptions.BadDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final UserService userService;
    private final TalentSpecialCaseService talentSpecialCaseService;

    public void moderate(ModerateUserRequestDto request) {
        log.debug("Starting moderation process for request: {}", request);

        if (isUserRegistrationModeration(request)) {
            userService.moderateUserAccount(request);
        } else if (isTalentSpecialCaseModeration(request)) {
            talentSpecialCaseService.moderateSpecialCase(request);
        } else {
            throw new BadDataException(
                    "Invalid moderation with type: " + request.getModerationType() +
                            ", and file type: " + request.getFileType()
            );
        }

        log.debug("Completed moderation process for request: {}", request);
    }

    private boolean isUserRegistrationModeration(ModerateUserRequestDto request) {
        return isExpectedFileType(request, "REGISTRATION_MEDIA", "REGISTRATION_DOCUMENT") &&
                isExpectedModerationType(request, ModerationTypeEnum.USER_REGISTRATION);
    }

    private boolean isTalentSpecialCaseModeration(ModerateUserRequestDto request) {
        return isExpectedFileType(request, "SPECIAL_CASE_DOCUMENT") &&
                isExpectedModerationType(request, ModerationTypeEnum.DOCUMENT_VERIFICATION);
    }

    private boolean isExpectedFileType(ModerateUserRequestDto request, String... expectedFileTypes) {
        for (String expectedType : expectedFileTypes) {
            if (expectedType.equals(request.getFileType())) {
                return true;
            }
        }
        return false;
    }

    private boolean isExpectedModerationType(ModerateUserRequestDto request, ModerationTypeEnum expectedType) {
        return expectedType.getName().equals(request.getModerationType());
    }
}