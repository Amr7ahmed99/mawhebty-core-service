package io.mawhebty.services;

import io.mawhebty.enums.*;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.exceptions.UserStatusNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
import org.springframework.stereotype.Service;
import io.mawhebty.exceptions.BadDataException;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final UserStatusRepository userStatusRepository;
    private final PostRepository postRepository;
    private final PostTypeRepository postTypeRepository;
    private final PostStatusRepository postStatusRepository;
    private final PostVisibilityRepository visibilityRepository;
    private final MediaModerationRepository mediaModerationRepository;
    private final MediaModerationStatusRepository mediaModerationStatusRepository;


    public Boolean validatePhone(String phone){
        if(phone.isEmpty()){
            throw new BadDataException("Phone number is empty");
        }

        return this.userRepository.findByPhone(phone).isPresent();
    }

    public Boolean validateEmail(String email){
        if(email.isEmpty()){
            throw new BadDataException("email is empty");
        }

        return this.userRepository.findByEmail(email).isPresent();
    }

    //TODO: Move activateUserAccount to userService.java
    // After approval on first uploaded file (el function de h3mlha call ba3d ma el php moderation service
    // tb3tly 3ala http api we tb2a approved moderation)
    public void activateUserAccount(Long userId, Long moderatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserStatus activeStatus = userStatusRepository.findByName(UserStatusEnum.ACTIVE.getName())
                .orElseThrow(() -> new UserStatusNotFoundException("ACTIVE status not found"));

        user.setStatus(activeStatus);
        userRepository.save(user);

        //active first post and media moderation
        PostType registrationType = postTypeRepository.findByName(PostTypeEnum.REGISTRATION_FILE.getName())
                .orElseThrow(() -> new ResourceNotFoundException("REGISTRATION_FILE type not found"));

        PostStatus published = postStatusRepository.findByName(PostStatusEnum.PUBLISHED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("PUBLISHED post status not found"));

        PostVisibility publicVisibility = visibilityRepository.findByName(PostVisibilityEnum.PUBLIC.getName())
                .orElseThrow(() -> new ResourceNotFoundException("PRIVATE visibility not found"));

        Post userRegisterationPost= postRepository.findByOwnerUserIdAndType(userId, registrationType)
                .orElseThrow(()-> new ResourceNotFoundException("User file not found"));

        userRegisterationPost.setStatus(published);
        userRegisterationPost.setVisibility(publicVisibility);

        // change media moderation to active
        MediaModerationStatus approvedStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.APPROVED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("APPROVED status not found"));


        userRegisterationPost.getMediaModeration().setCheckedAt(LocalDateTime.now());
        userRegisterationPost.getMediaModeration().setStatus(approvedStatus);
        if(moderatorId != null){
            userRegisterationPost.getMediaModeration().setModeratorId(moderatorId);
        }
    }
}
