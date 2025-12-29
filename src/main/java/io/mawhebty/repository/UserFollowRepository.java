package io.mawhebty.repositories;

import io.mawhebty.enums.FollowStatus;
import io.mawhebty.models.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<UserFollow> findByFollowerIdAndStatus(Long followerId, FollowStatus status);

    List<UserFollow> findByFollowingIdAndStatus(Long followingId, FollowStatus status);

    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following.id = :userId AND uf.status = 'ACTIVE'")
    Long countFollowersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.id = :userId AND uf.status = 'ACTIVE'")
    Long countFollowingByUserId(@Param("userId") Long userId);

    boolean existsByFollowerIdAndFollowingIdAndStatus(Long followerId, Long followingId, FollowStatus status);
}