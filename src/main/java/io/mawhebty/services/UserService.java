package io.mawhebty.services;

import io.mawhebty.dtos.FindOrCreateUserDto;
import io.mawhebty.dtos.requests.InternalServices.ModerateUserRequestDto;
import io.mawhebty.enums.*;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.exceptions.UserStatusNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.projections.UserProfileProjection;
import io.mawhebty.repository.*;
import io.mawhebty.support.MessageService;
import jakarta.transaction.Transactional;
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
    private final MessageService messageService;

    public Boolean validatePhone(String fullPhone){
        fullPhone= fullPhone.trim();
        if(fullPhone.isBlank()){
            throw new BadDataException(messageService.getMessage("phone.is.empty"));
        }
//        if(!fullPhone.contains("+")){
//            fullPhone= "+" + fullPhone;
//        }
        return this.userRepository.findByFullPhone(fullPhone).isPresent();
    }

    public Boolean validateEmail(String email){
        if(email.isEmpty()){
            throw new BadDataException(messageService.getMessage("email.is.empty"));
        }

        return this.userRepository.findByEmail(email).isPresent();
    }

    public User findByEmail(String email){
        return this.userRepository.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found by email: "+email));
    }

    //TODO: Move activateUserAccount to userService.java
    // After approval on first uploaded file (el function de h3mlha call ba3d ma el php moderation service
    // tb3tly 3ala http api we tb2a approved moderation)
    @Transactional(rollbackOn = {Exception.class})
    public void moderateUserAccount(ModerateUserRequestDto req){
        switch (req.getDecision()){
//            case "APPROVED":
            case "approved":
                this.approveUserAccount(req.getUserId(), req.getMediaId(), req.getModeratorId());
                break;
//            case "REJECTED":
            case "rejected":
                this.rejectUserAccount(req);
                break;
            default:
                throw new BadDataException("Invalid status: "+ req.getDecision());
        }
    }

    public void approveUserAccount(Long userId, Long postId, Long moderatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if(!user.getStatus().getName().equals(UserStatusEnum.PENDING_MODERATION.getName())){
            throw new IllegalStateException("Can not approve user at current state: "+user.getStatus().getName());
        }

        UserStatus activeStatus = userStatusRepository.findByName(UserStatusEnum.ACTIVE.getName())
                .orElseThrow(() -> new UserStatusNotFoundException("ACTIVE status not found"));

        user.setStatus(activeStatus);
        userRepository.save(user);

        //active first post and media moderation
        PostStatus published = postStatusRepository.findByName(PostStatusEnum.PUBLISHED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("PUBLISHED post status not found"));

        PostVisibility publicVisibility = visibilityRepository.findByName(PostVisibilityEnum.PUBLIC.getName())
                .orElseThrow(() -> new ResourceNotFoundException("PUBLIC visibility not found"));

        Post userRegisterationPost= postRepository.findByIdAndOwnerUserIdAndTypeId(postId, userId, PostTypeEnum.REGISTRATION_FILE.getId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        String.format("Registration file not found for user: %d, post: %d", userId, postId)
                ));

        userRegisterationPost.setStatus(published);
        userRegisterationPost.setVisibility(publicVisibility);

        // change media moderation to active
        MediaModerationStatus approvedStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.APPROVED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("APPROVED status not found"));


        if(userRegisterationPost.getMediaModeration() == null){
            throw new IllegalStateException("Media moderation record not found for post: "+ postId);
        }

        userRegisterationPost.getMediaModeration().setCheckedAt(LocalDateTime.now());
        userRegisterationPost.getMediaModeration().setStatus(approvedStatus);
        if(moderatorId != null){
            userRegisterationPost.getMediaModeration().setModeratorId(moderatorId);
        }

        this.postRepository.save(userRegisterationPost);
    }

    public void rejectUserAccount(ModerateUserRequestDto req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new UserNotFoundException(req.getUserId()));

        if(!user.getStatus().getName().equals(UserStatusEnum.PENDING_MODERATION.getName())){
            throw new IllegalStateException("Can not reject user at current state: "+user.getStatus().getName());
        }

        if(req.getReason() == null || req.getReason().isBlank()){
            throw new BadDataException("Rejection reason must not be null");
        }

        UserStatus userRejectedStatus = userStatusRepository.findByName(UserStatusEnum.REJECTED.getName())
                .orElseThrow(() -> new UserStatusNotFoundException("REJECTED status not found"));

        user.setStatus(userRejectedStatus);
        userRepository.save(user);

        //active first post and media moderation
        Post userRegisterationPost= postRepository.findByIdAndOwnerUserIdAndTypeId(req.getMediaId(), req.getUserId(), PostTypeEnum.REGISTRATION_FILE.getId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        String.format("Registration file not found for user: %d, post: %d", req.getUserId(), req.getMediaId())
                ));

        PostStatus postRejectedStatus = postStatusRepository.findByName(PostStatusEnum.REJECTED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("REJECTED post status not found"));

        userRegisterationPost.setStatus(postRejectedStatus);

        // change media moderation to rejected
        MediaModerationStatus mediaRejectedStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.REJECTED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("REJECTED status not found"));

        if(userRegisterationPost.getMediaModeration() == null){
            throw new IllegalStateException("Media moderation record not found for post: "+ req.getMediaId());
        }

        userRegisterationPost.getMediaModeration().setCheckedAt(LocalDateTime.now());
        userRegisterationPost.getMediaModeration().setStatus(mediaRejectedStatus);
        userRegisterationPost.getMediaModeration().setReason(req.getReason());
        if(req.getModeratorId() != null){
            userRegisterationPost.getMediaModeration().setModeratorId(req.getModeratorId());
        }

        this.postRepository.save(userRegisterationPost);
    }

    public FindOrCreateUserDto findOrCreateByEmail(String email){
        boolean isNewUser = false;

        // check if the user is new or already registered
        User user = userRepository.findByEmailFetchStatus(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .isVerified(false)
                        .email(email)
                        .status(userStatusRepository.findByName(UserStatusEnum.DRAFT.getName())
                                .orElseThrow(() -> new ResourceNotFoundException("DRAFT status not found")))
                        .build())
                );

        // check if it has profile, because we create a profile for the user after draft registration step
        UserProfileProjection profileProj = userRepository.checkUserHasProfileById(user.getId());
        if (!profileProj.hasProfile()) {
            isNewUser = true;
        }

        return FindOrCreateUserDto.builder()
                .isNew(isNewUser)
                .user(user)
                .build();
    }
}
