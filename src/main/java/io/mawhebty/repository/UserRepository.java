package io.mawhebty.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.User;
import io.mawhebty.projections.UserProfileProjection;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    Optional<User> findByPhone(String phone);

    @Query("SELECT u FROM User u JOIN FETCH u.status WHERE u.email = :email")
    Optional<User> findByEmailFetchStatus(String email);


    @Query(value = """
        SELECT 
            tp.id AS talentProfileId, 
            rp.id AS researcherProfileId 
        FROM users u 
        LEFT JOIN talent_profile tp ON tp.user_id = u.id
        LEFT JOIN researcher_profile rp ON rp.user_id = u.id 
        WHERE u.id = :userId
        """, nativeQuery = true)
    UserProfileProjection checkUserHasProfileById(@Param("userId") Long userId);
}