package io.mawhebty.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.mawhebty.repository.MediaModerationRepository;
import io.mawhebty.repository.PostRepository;
import io.mawhebty.repository.UserRepository;
import io.mawhebty.repository.UserStatusRepository;

@Slf4j
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final PostRepository postRepository;
    private final MediaModerationRepository mediaModerationRepository;
    private final S3Service s3Service;
    private final JWTService jwtService;
    private final EmailService emailService;

    public AdminService(UserRepository userRepository, 
                       UserStatusRepository userStatusRepository,
                       PostRepository postRepository,
                       MediaModerationRepository mediaModerationRepository,
                       S3Service s3Service,
                       JWTService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.userStatusRepository = userStatusRepository;
        this.postRepository = postRepository;
        this.mediaModerationRepository = mediaModerationRepository;
        this.s3Service = s3Service;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Send file to admin sqs for manual review
     */
//    public boolean sendFileToAdminReviewSQS(Long userId, String fileUrl) {
//        try {
//
//            // send to dashboard
//
//            log.info("File sent to admin dashboard for review: {}", fileUrl);
//
//            // momken ab2a a3ml add some logic to:
//            // 1. Create admin notification
//            // 2. Send email to admins
//            return true;
//        } catch (Exception e) {
//            log.error("Failed to send file to admin dashboard: {}", e.getMessage());
//            throw new RuntimeException("Failed to send file for admin review", e);
//        }
//    }

    /**
     * Review user registration (manual approval for companies/documents)
     */
    // public AdminReviewResponse reviewUserRegistration(Long userId, AdminReviewRequest request) {
    //     try {
    //         User user = userRepository.findById(userId)
    //                 .orElseThrow(() -> new UserNotFoundException(userId));

    //         UserStatus newStatus = userStatusRepository.findByName(request.getStatus().name())
    //                 .orElseThrow(() -> new RuntimeException("Status not found"));

    //         user.setStatus(newStatus);
    //         userRepository.save(user);

    //         // Send notification to user
    //         if (request.getStatus() == UserStatusEnum.ACTIVE) {
    //             emailService.sendAccountApprovedEmail(user.getEmail(), user.getFullName());
    //         } else if (request.getStatus() == UserStatusEnum.REJECTED) {
    //             emailService.sendAccountRejectedEmail(user.getEmail(), request.getRejectionReason());
    //         }

    //         log.info("User {} registration reviewed. New status: {}", userId, request.getStatus());

    //         return AdminReviewResponse.builder()
    //                 .success(true)
    //                 .userId(userId)
    //                 .newStatus(request.getStatus())
    //                 .message("User registration reviewed successfully")
    //                 .reviewedAt(LocalDateTime.now())
    //                 .build();

    //     } catch (Exception e) {
    //         log.error("Error reviewing user registration for user {}: {}", userId, e.getMessage());
    //         return AdminReviewResponse.builder()
    //                 .success(false)
    //                 .userId(userId)
    //                 .message("Failed to review user registration: " + e.getMessage())
    //                 .build();
    //     }
    // }

    // /**
    //  * Review media content (posts, profile pictures, etc.)
    //  */
    // public AdminReviewResponse reviewMediaContent(Long mediaId, AdminReviewRequest request) {
    //     try {
    //         MediaModeration mediaModeration = mediaModerationRepository.findById(mediaId)
    //                 .orElseThrow(() -> new RuntimeException("Media moderation record not found"));

    //         mediaModeration.setStatus(request.getStatus());
    //         mediaModeration.setReason(request.getRejectionReason());
    //         mediaModeration.setCheckedAt(LocalDateTime.now());
    //         mediaModerationRepository.save(mediaModeration);

    //         // If media is approved, update post status
    //         if (request.getStatus() == MediaModerationStatusEnum.APPROVED) {
    //             Post post = mediaModeration.getPost();
    //             // Update post to published status if needed
    //             // post.setStatus(publishedStatus);
    //             postRepository.save(post);
    //         }

    //         log.info("Media {} reviewed. Status: {}", mediaId, request.getStatus());

    //         return AdminReviewResponse.builder()
    //                 .success(true)
    //                 .mediaId(mediaId)
    //                 .newStatus(request.getStatus())
    //                 .message("Media content reviewed successfully")
    //                 .reviewedAt(LocalDateTime.now())
    //                 .build();

    //     } catch (Exception e) {
    //         log.error("Error reviewing media content for media {}: {}", mediaId, e.getMessage());
    //         return AdminReviewResponse.builder()
    //                 .success(false)
    //                 .mediaId(mediaId)
    //                 .message("Failed to review media content: " + e.getMessage())
    //                 .build();
    //     }
    // }

    // /**
    //  * Get pending registrations for admin review
    //  */
    // public List<User> getPendingRegistrations() {
    //     return userRepository.findByStatusName(UserStatusEnum.PENDING_MODERATION.name());
    // }

    // /**
    //  * Get pending media for admin review
    //  */
    // public List<MediaModeration> getPendingMediaReviews() {
    //     return mediaModerationRepository.findByStatus(MediaModerationStatusEnum.PENDING);
    // }

    // /**
    //  * Suspend user account
    //  */
    // public boolean suspendUser(Long userId, String reason) {
    //     try {
    //         User user = userRepository.findById(userId)
    //                 .orElseThrow(() -> new UserNotFoundException(userId));

    //         UserStatus suspendedStatus = userStatusRepository.findByName(UserStatusEnum.SUSPENDED.name())
    //                 .orElseThrow(() -> new RuntimeException("Suspended status not found"));

    //         user.setStatus(suspendedStatus);
    //         userRepository.save(user);

    //         // Notify user
    //         emailService.sendAccountSuspendedEmail(user.getEmail(), reason);

    //         log.info("User {} suspended. Reason: {}", userId, reason);
    //         return true;

    //     } catch (Exception e) {
    //         log.error("Error suspending user {}: {}", userId, e.getMessage());
    //         return false;
    //     }
    // }

    // /**
    //  * Activate suspended user
    //  */
    // public boolean activateUser(Long userId) {
    //     try {
    //         User user = userRepository.findById(userId)
    //                 .orElseThrow(() -> new UserNotFoundException(userId));

    //         UserStatus activeStatus = userStatusRepository.findByName(UserStatusEnum.ACTIVE.name())
    //                 .orElseThrow(() -> new RuntimeException("Active status not found"));

    //         user.setStatus(activeStatus);
    //         userRepository.save(user);

    //         // Notify user
    //         emailService.sendAccountReactivatedEmail(user.getEmail());

    //         log.info("User {} activated", userId);
    //         return true;

    //     } catch (Exception e) {
    //         log.error("Error activating user {}: {}", userId, e.getMessage());
    //         return false;
    //     }
    // }

    // /**
    //  * Get user statistics for admin dashboard
    //  */
    // public AdminDashboardStats getDashboardStats() {
    //     long totalUsers = userRepository.count();
    //     long pendingRegistrations = userRepository.countByStatusName(UserStatusEnum.PENDING_MODERATION.name());
    //     long activeUsers = userRepository.countByStatusName(UserStatusEnum.ACTIVE.name());
    //     long suspendedUsers = userRepository.countByStatusName(UserStatusEnum.SUSPENDED.name());
    //     long pendingMedia = mediaModerationRepository.countByStatus(MediaModerationStatusEnum.PENDING);

    //     return AdminDashboardStats.builder()
    //             .totalUsers(totalUsers)
    //             .pendingRegistrations(pendingRegistrations)
    //             .activeUsers(activeUsers)
    //             .suspendedUsers(suspendedUsers)
    //             .pendingMediaReviews(pendingMedia)
    //             .build();
    // }
}