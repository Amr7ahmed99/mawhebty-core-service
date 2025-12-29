package io.mawhebty.services;

import io.mawhebty.enums.FollowStatus;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.models.User;
import io.mawhebty.models.UserFollow;
import io.mawhebty.repositories.UserFollowRepository;
import io.mawhebty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BadDataException("Cannot follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Follower not found"));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Following user not found"));

        // Check if already following
        userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .ifPresent(existingFollow -> {
                    if (existingFollow.getStatus() == FollowStatus.ACTIVE) {
                        throw new BadDataException("Already following this user");
                    }
                    // If previously unfollowed, reactivate
                    existingFollow.setStatus(FollowStatus.ACTIVE);
                    existingFollow.setFollowedAt(LocalDateTime.now());
                    existingFollow.setUnfollowedAt(null);
                    userFollowRepository.save(existingFollow);
                    throw new BadDataException("Follow relationship reactivated");
                });

        // Create new follow relationship
        UserFollow userFollow = UserFollow.builder()
                .follower(follower)
                .following(following)
                .status(FollowStatus.ACTIVE)
                .followedAt(LocalDateTime.now())
                .build();
        userFollowRepository.save(userFollow);
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        UserFollow userFollow = userFollowRepository
                .findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow relationship not found"));

        userFollow.setStatus(FollowStatus.UNFOLLOWED);
        userFollow.setUnfollowedAt(LocalDateTime.now());
        userFollowRepository.save(userFollow);
    }

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BadDataException("Cannot block yourself");
        }

        UserFollow userFollow = userFollowRepository
                .findByFollowerIdAndFollowingId(blockerId, blockedId)
                .orElse(UserFollow.builder()
                        .follower(userRepository.findById(blockerId).orElseThrow())
                        .following(userRepository.findById(blockedId).orElseThrow())
                        .build());

        userFollow.setStatus(FollowStatus.BLOCKED);
        userFollow.setUnfollowedAt(LocalDateTime.now());
        userFollowRepository.save(userFollow);
    }

    public Map<String, Long> getFollowCounts(Long userId) {
        Long followersCount = userFollowRepository.countFollowersByUserId(userId);
        Long followingCount = userFollowRepository.countFollowingByUserId(userId);

        return Map.of(
                "followers", followersCount,
                "following", followingCount
        );
    }

    public List<UserFollow> getUserFollowers(Long userId) {
        return userFollowRepository.findByFollowingIdAndStatus(userId, FollowStatus.ACTIVE);
    }

    public List<UserFollow> getUserFollowing(Long userId) {
        return userFollowRepository.findByFollowerIdAndStatus(userId, FollowStatus.ACTIVE);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.existsByFollowerIdAndFollowingIdAndStatus(
                followerId, followingId, FollowStatus.ACTIVE);
    }
}