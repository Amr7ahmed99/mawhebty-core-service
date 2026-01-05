package io.mawhebty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByOwnerUserId(Long ownerUserId);

    Optional<Post> findByIdAndOwnerUserIdAndTypeId(Long postId, Long ownerUserId, Integer type);

    // Get posts by owner with pagination
    @Query(value = "SELECT * FROM posts WHERE owner_user_id = :ownerId " +
            "ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Post> findByOwnerIdWithPagination(@Param("ownerId") Long ownerId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    // Get published posts by owner with pagination
    @Query(value = "SELECT p.* FROM posts p " +
            "LEFT JOIN post_statuses ps ON p.status_id = ps.id " +
            "WHERE p.owner_user_id = :ownerId AND ps.name = 'PUBLISHED' " +
            "ORDER BY p.created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Post> findPublishedPostsByOwnerId(@Param("ownerId") Long ownerId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    // Get all published posts with pagination
    @Query(value = "SELECT p.* FROM posts p " +
            "LEFT JOIN post_statuses ps ON p.status_id = ps.id " +
            "WHERE ps.name = 'PUBLISHED' " +
            "ORDER BY p.created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Post> findPublishedPosts(@Param("limit") int limit,
                                  @Param("offset") int offset);

    // Get approved posts by user (with moderation approved)
    @Query(value = "SELECT p.* FROM posts p " +
            "LEFT JOIN media_moderations mm ON p.media_moderation_id = mm.id " +
            "LEFT JOIN media_moderation_statuses mms ON mm.status_id = mms.id " +
            "WHERE p.owner_user_id = :userId AND mms.name = 'APPROVED' " +
            "ORDER BY p.created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Post> findApprovedPostsByUserId(@Param("userId") Long userId,
                                         @Param("limit") int limit,
                                         @Param("offset") int offset);

    // Count queries
    @Query(value = "SELECT COUNT(*) FROM posts WHERE owner_user_id = :ownerId",
            nativeQuery = true)
    Long countByOwnerId(@Param("ownerId") Long ownerId);

    @Query(value = "SELECT COUNT(*) FROM posts p " +
            "LEFT JOIN post_statuses ps ON p.status_id = ps.id " +
            "WHERE p.owner_user_id = :ownerId AND ps.name = 'PUBLISHED'",
            nativeQuery = true)
    Long countPublishedByOwnerId(@Param("ownerId") Long ownerId);

    // Find posts by IDs
    @Query(value = "SELECT * FROM posts WHERE id IN :ids",
            nativeQuery = true)
    List<Post> findByIds(@Param("ids") List<Long> ids);

    // Find post by ID and owner
    @Query(value = "SELECT * FROM posts WHERE id = :id AND owner_user_id = :ownerId",
            nativeQuery = true)
    Optional<Post> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    // Find post by ID and type
    @Query(value = "SELECT * FROM posts WHERE id = :id AND owner_user_id = :ownerId AND type_id = :typeId",
            nativeQuery = true)
    Optional<Post> findByIdAndOwnerIdAndTypeId(@Param("id") Long id,
                                               @Param("ownerId") Long ownerId,
                                               @Param("typeId") Long typeId);

    @Query("""
        SELECT p
        FROM Post p
        WHERE p.ownerUser.id <> :ownerId
          AND p.status.id = :publishedStatusId
          AND p.visibility.id = :publicVisibilityId
          AND p.category.id = :categoryId
          AND ( :subCategoryId IS NULL OR p.subCategory.id = :subCategoryId )
    """)
        Page<Post> findPublicByOwnerIdAndCategoryIdSubCategoryId(
                @Param("ownerId") Long ownerId,
                @Param("publishedStatusId") Integer publishedStatusId,
                @Param("publicVisibilityId") Integer publicVisibilityId,
                @Param("categoryId") Integer categoryId,
                @Param("subCategoryId") Integer subCategoryId,
                Pageable pageable
        );

}
