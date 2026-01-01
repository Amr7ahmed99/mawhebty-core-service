package io.mawhebty.repository;

import io.mawhebty.enums.SavedItemTypeEnum;
import io.mawhebty.models.SavedItem;
import io.mawhebty.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {

    // Native query with LIMIT and OFFSET
    @Query(value = "SELECT * FROM saved_items WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<SavedItem> findByUserIdWithPagination(@Param("userId") Long userId,
                                               @Param("limit") int limit,
                                               @Param("offset") int offset);

    // Filter by item type with pagination
    @Query(value = "SELECT * FROM saved_items WHERE user_id = :userId AND item_type_id = :itemType " +
            "ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<SavedItem> findByUserIdAndItemTypeWithPagination(@Param("userId") Long userId,
                                                          @Param("itemType") int itemType,
                                                          @Param("limit") int limit,
                                                          @Param("offset") int offset);

  // Get saved posts with owner details
    @Query(value = "SELECT si.* FROM saved_items si " +
            "LEFT JOIN posts p ON si.item_id = p.id " +
            "LEFT JOIN users u ON p.owner_user_id = u.id " +
            "LEFT JOIN post_statuses ps ON p.status_id = ps.id " +
            "WHERE si.user_id = :userId AND si.item_type_id = 1 " +
            "AND ps.name = 'PUBLISHED' " +
            "ORDER BY si.created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<SavedItem> findSavedPostsWithOwner(@Param("userId") Long userId,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    // Get saved events
    @Query(value = "SELECT si.* FROM saved_items si " +
            "LEFT JOIN events e ON si.item_id = e.id " +
            "WHERE si.user_id = :userId AND si.item_type_id = 2 " +
            "AND e.status != 'CANCELLED' " +
            "ORDER BY si.created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<SavedItem> findSavedEvents(@Param("userId") Long userId,
                                    @Param("limit") int limit,
                                    @Param("offset") int offset);

    // Get saved articles
    @Query(value = "SELECT si.* FROM saved_items si " +
            "LEFT JOIN articles a ON si.item_id = a.id " +
            "WHERE si.user_id = :userId AND si.item_type_id = 3 " +
            "AND a.status = 'PUBLISHED' " +
            "ORDER BY si.created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<SavedItem> findSavedArticles(@Param("userId") Long userId,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    // Count queries
    @Query(value = "SELECT COUNT(*) FROM saved_items WHERE user_id = :userId",
            nativeQuery = true)
    Long countByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM saved_items WHERE user_id = :userId AND item_type_id = :itemType",
            nativeQuery = true)
    Long countByUserIdAndItemType(@Param("userId") Long userId, @Param("itemType") int itemType);

    // Find specific saved item
    Optional<SavedItem> findByUserAndItemTypeAndItemId(User user, SavedItemTypeEnum itemType, Long itemId);

    // Check if item is saved
    boolean existsByUserAndItemTypeAndItemId(User user, SavedItemTypeEnum itemType, Long itemId);

    // Delete saved item
    void deleteByUserAndItemTypeAndItemId(User user, SavedItemTypeEnum itemType, Long itemId);
}