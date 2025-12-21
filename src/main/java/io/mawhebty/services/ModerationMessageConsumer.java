package io.mawhebty.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationMessageConsumer {

    private final SqsService sqsService;
    private final ModerationQueueService moderationService;

//    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    public void processModerationMessages() {
        log.info("Checking for moderation messages...");
        
        // List<Message> messages = sqsService.receiveModerationMessages();
        
        // for (Message message : messages) {
        //     try {
        //         ModerationPayload payload = sqsService.processModerationMessage(message);
                
        //         if (payload != null) {
        //             // Process the moderation request
        //             moderationService.processModerationRequest(payload);
                    
        //             // Delete message from queue after successful processing
        //             sqsService.deleteMessage(sqsService.getModerationQueueUrl(), message.receiptHandle());
        //         }
                
        //     } catch (Exception e) {
        //         log.error("Failed to process moderation message: {}", e.getMessage());
        //         // Message will remain in queue for retry
        //     }
        // }
    }
}