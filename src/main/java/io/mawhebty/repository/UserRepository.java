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

    @Query("SELECT u FROM User u WHERE CONCAT(u.countryCode, u.phoneNumber) = :fullPhone")
    Optional<User> findByFullPhone(@Param("fullPhone") String fullPhone);


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.status WHERE u.email = :email")
    Optional<User> findByEmailFetchStatus(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.status LEFT JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailFetchStatusAndRole(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.status LEFT JOIN FETCH u.userType WHERE u.email = :email")
    Optional<User> findByEmailFetchStatusAndUserType(String email);


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role LEFT JOIN FETCH u.status LEFT JOIN FETCH u.userType WHERE u.id = :id")
    Optional<User> findByIdFetchRoleAndStatusAndUserType(@Param("id") Long id);


    @Query(value = """
        SELECT 
            tp.id AS talentProfileId, 
            crp.id AS companyResearcherProfileId,
            irp.id AS individualResearcherProfileId
        FROM users u 
        LEFT JOIN talent_profile tp ON tp.user_id = u.id
        LEFT JOIN company_researcher_profile crp ON crp.user_id = u.id 
        LEFT JOIN individual_researcher_profile irp ON irp.user_id = u.id 
        WHERE u.id = :userId
        """, nativeQuery = true)
    UserProfileProjection checkUserHasProfileById(@Param("userId") Long userId);
}