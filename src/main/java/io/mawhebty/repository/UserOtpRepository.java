package io.mawhebty.repository;

import io.mawhebty.models.UserOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOTP, Long> {

    // Find active OTP by user ID and type
//     @Query("SELECT otp FROM UserOTP otp " +
//            "WHERE otp.user.id = :userId " +
//            "AND otp.otpType.name = :otpType " +
//            "AND otp.isUsed = :isUsed " +
//            "AND otp.expiryDate > :currentTime " +
//            "ORDER BY otp.createdAt DESC")
//     Optional<UserOTP> findActiveByUserIdAndTypeName(
//             @Param("userId") Long userId,
//             @Param("otpType") OTPTypeEnum otpType,
//             @Param("isUsed") Boolean isUsed,
//             @Param("currentTime") LocalDateTime currentTime);

//     // Default method with current time
//     default Optional<UserOTP> findActiveByUserIdAndTypeName(Long userId, OTPTypeEnum otpType, Boolean isUsed) {
//         return findActiveByUserIdAndTypeName(userId, otpType, isUsed, LocalDateTime.now());
//     }


@Query("SELECT otp FROM UserOTP otp " +
           "WHERE otp.user.id = :userId " +
           "AND otp.isUsed = :isUsed " +
           "AND otp.expiryDate > :currentTime " +
           "ORDER BY otp.createdAt DESC")
    Optional<UserOTP> findActiveByUserIdAndTypeName(
            @Param("userId") Long userId,
            @Param("isUsed") Boolean isUsed,
            @Param("currentTime") LocalDateTime currentTime);

    // Default method with current time
    default Optional<UserOTP> findActiveByUserIdAndTypeName(Long userId, Boolean isUsed) {
        return findActiveByUserIdAndTypeName(userId, isUsed, LocalDateTime.now());
    }

    // Find all active OTPs for a user (for cleanup)
    @Query("SELECT otp FROM UserOTP otp " +
           "WHERE otp.user.id = :userId " +
           "AND otp.expiryDate > :currentTime " +
           "AND otp.isUsed = false")
    List<UserOTP> findAllActiveByUserId(@Param("userId") Long userId, 
                                       @Param("currentTime") LocalDateTime currentTime);

    // Count active OTPs for a user (prevent spam)
    @Query("SELECT COUNT(otp) FROM UserOTP otp " +
           "WHERE otp.user.id = :userId " +
           "AND otp.expiryDate > :currentTime " +
           "AND otp.isUsed = false")
    Long countActiveOtpsByUserId(@Param("userId") Long userId, 
                                @Param("currentTime") LocalDateTime currentTime);

    // Find by OTP code (for verification)
    @Query("SELECT otp FROM UserOTP otp " +
           "WHERE otp.hashedCode = :hashedCode " +
           "AND otp.expiryDate > :currentTime " +
           "AND otp.isUsed = false")
    Optional<UserOTP> findByHashedCodeAndActive(@Param("hashedCode") String hashedCode,
                                               @Param("currentTime") LocalDateTime currentTime);

    // Cleanup expired OTPs
    @Query("DELETE FROM UserOTP otp WHERE otp.expiryDate < :expiryTime")
    void deleteExpiredOtps(@Param("expiryTime") LocalDateTime expiryTime);
}