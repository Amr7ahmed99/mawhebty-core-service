package io.mawhebty.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.mawhebty.dtos.requests.payload.ModerationPayload;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationQueueService {

    private final SqsService sqsService;
//    private final TalentProfileRepository talentProfileRepository;
//    private final ResearcherProfileRepository researcherProfileRepository;


    // public void sendToModeration(User user, ModerationTypeEnum moderationType) {
    //     try {
    //         // 1. Prepare moderation payload
    //         ModerationPayload payload = new ModerationPayload();
    //         payload.setUserId(user.getId());
    //         payload.setUserRole(user.getRole());
    //         payload.setSubmittedAt(LocalDateTime.now());
    //         payload.setModerationType(moderationType);
            
    //         // 2. Get file URL based on role
    //         String fileUrl = getProfileFileUrl(user);
    //         payload.setFileUrl(fileUrl);
    //         payload.setFileType("PROFILE_PICTURE");
            
    //         // 3. Send to SQS queue
    //         boolean sent = sqsService.sendToModerationQueue(payload);
            
    //         if (sent) {
    //             log.info("User {} sent to moderation queue successfully", user.getId());
    //         } else {
    //             log.error("Failed to send user {} to moderation queue", user.getId());
    //             throw new RuntimeException("Failed to send user to moderation queue");
    //         }

    //     } catch (Exception e) {
    //         log.error("Error sending user {} to moderation: {}", user.getId(), e.getMessage());
    //         throw new RuntimeException("Failed to process moderation request", e);
    //     }
    // }

    /**
     * Send file content for moderation
     */
    public boolean sendFileForModeration(Long userId, Integer roleId, String fileType, String moderationType,
                                            Long mediaId, String mediaUrl) {

        try {
            ModerationPayload payload = new ModerationPayload();
            payload.setUserId(userId);
            payload.setMediaId(mediaId);
            payload.setUserRole(roleId);
            payload.setFileUrl(mediaUrl);
            payload.setFileType(fileType);
            payload.setSubmittedAt(LocalDateTime.now());
            payload.setModerationType(moderationType);

            boolean sent = sqsService.sendToModerationQueue(payload);
            
            if (sent) {
                log.info("File {} sent to moderation queue for user {}", mediaUrl, userId);
            } else {
                log.error("Failed to send media to moderation queue for user {}", userId);
            }

            return sent;
        } catch (Exception e) {
            log.error("Error sending media to moderation for user {}: {}", userId, e.getMessage());
        }

        return false;
    }

    /**
     * Send document for verification
     */
//    public boolean sendDocumentForVerification(Long userId, String fileType, String moderationType, Long postId, String documentUrl) {
//        boolean sent= false;
//        try {
//            ModerationPayload payload = new ModerationPayload();
//            payload.setUserId(userId);
//            payload.setFileUrl(documentUrl);
//            payload.setMediaId(postId);
//            payload.setFileType(fileType);
//            payload.setSubmittedAt(LocalDateTime.now());
//            payload.setModerationType(moderationType);
//
//            sent = sqsService.sendToModerationQueue(payload);
//
//            if (sent) {
//                log.info("Document {} sent for verification for user {}", documentUrl, userId);
//            } else {
//                log.error("Failed to send document for verification for user {}", userId);
//            }
//        } catch (Exception e) {
//            log.error("Error sending document for verification for user {}: {}", userId, e.getMessage());
//        }
//        return sent;
//    }

    // private String getProfileFileUrl(User user) {
    //     if (user.getRole().getName().equals(UserRoleEnum.TALENT.name())) {
    //         TalentProfile profile = talentProfileRepository.findByUserId(user.getId())
    //             .orElseThrow(() -> new NotFoundException("Talent profile not found for user: " + user.getId()));
    //         return profile.getProfilePicture();
    //     } else {
    //         ResearcherProfile profile = researcherProfileRepository.findByUserId(user.getId())
    //             .orElseThrow(() -> new NotFoundException("Researcher profile not found for user: " + user.getId()));
    //         return profile.getProfilePicture();
    //     }
    // }
}