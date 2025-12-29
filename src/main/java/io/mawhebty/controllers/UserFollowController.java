package io.mawhebty.controllers;

import io.mawhebty.api.v1.mawhebty.platform.AbstractMawhebtyPlatformController;
import io.mawhebty.api.v1.mawhebtyPlatform.MawhebtyPlatformUserFollowApi;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.UserFollowResponseResource;
import io.mawhebty.models.*;
import io.mawhebty.services.UserFollowService;
import io.mawhebty.services.auth.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController("MawhebtyPlatformUserFollowController")
@RequiredArgsConstructor
public class UserFollowController extends AbstractMawhebtyPlatformController
        implements MawhebtyPlatformUserFollowApi {

    private final UserFollowService userFollowService;
    private final CurrentUserService currentUserService;

    @Override
    public ResponseEntity<Void> followUser(Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = currentUserService.getCurrentUserId(authentication);
        userFollowService.followUser(currentUserId, userId.longValue());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> unfollowUser(Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = currentUserService.getCurrentUserId(authentication);
        userFollowService.unfollowUser(currentUserId, userId.longValue());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<UserFollowResponseResource>> getUserFollowers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = currentUserService.getCurrentUserId(authentication);
        List<UserFollow> userFollows = userFollowService.getUserFollowers(currentUserId);

        List<UserFollowResponseResource> followers = userFollows.stream()
                .map(userFollow -> mapToFollowResponse(userFollow, true)) // true = follower
                .collect(Collectors.toList());

        return ResponseEntity.ok(followers);
    }

    @Override
    public ResponseEntity<List<UserFollowResponseResource>> getUserFollowing() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = currentUserService.getCurrentUserId(authentication);
        List<UserFollow> userFollows = userFollowService.getUserFollowing(currentUserId);

        List<UserFollowResponseResource> following = userFollows.stream()
                .map(userFollow -> mapToFollowResponse(userFollow, false)) // false = following
                .collect(Collectors.toList());

        return ResponseEntity.ok(following);
    }

    private UserFollowResponseResource mapToFollowResponse(UserFollow userFollow, boolean isFollower) {
        UserFollowResponseResource response = new UserFollowResponseResource();

        User user = isFollower ? userFollow.getFollower() : userFollow.getFollowing();

        if (user != null) {
            response.setUserId(Math.toIntExact(user.getId()));

            String firstName = "";
            String lastName = "";
            String imageUrl = "";
            String shortBio = "";

            if (user.getTalentProfile() != null) {
                TalentProfile profile = user.getTalentProfile();
                firstName = profile.getFirstName();
                lastName = profile.getLastName();
                imageUrl = profile.getProfilePicture();
                shortBio = profile.getShortBio();
            } else if (user.getIndividualResearcherProfile() != null) {
                IndividualResearcherProfile profile = user.getIndividualResearcherProfile();
                firstName = profile.getFirstName();
                lastName = profile.getLastName();
                imageUrl = profile.getProfilePicture();
                shortBio = profile.getShortBio();
            } else if (user.getCompanyResearcherProfile() != null) {
                CompanyResearcherProfile profile = user.getCompanyResearcherProfile();
                firstName = profile.getCompanyName();
                lastName = "";
                imageUrl = profile.getProfilePicture();
                shortBio = profile.getShortBio();
            }

            response.setFirstName(firstName != null ? firstName : "");
            response.setLastName(lastName != null ? lastName : "");
            response.setImageUrl(imageUrl);
            response.setShortBio(shortBio);
        }

        response.setFollowedAt(userFollow.getFollowedAt());

        return response;
    }
}