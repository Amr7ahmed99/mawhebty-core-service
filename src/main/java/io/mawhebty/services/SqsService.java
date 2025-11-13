package io.mawhebty.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mawhebty.dtos.requests.payload.ModerationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.moderation-queue-url}")
    private String moderationQueueUrl;

    // @Value("${aws.sqs.notification-queue-url:}")
    // private String notificationQueueUrl;


    /**
     * Send message to moderation queue
     */
    public boolean sendToModerationQueue(ModerationPayload payload) {
        try {
            String messageBody = objectMapper.writeValueAsString(payload);
            
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(moderationQueueUrl)
                    .messageBody(messageBody)
                    // .messageGroupId(String.valueOf("moderation")) // 3la4an yb2a FIFO queue
                    // .messageDeduplicationId(payload.getUserId() + "-" + System.currentTimeMillis()) //nounce
                    .build();
            
            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            
            log.info("Message sent to moderation queue. Message ID: {}, User ID: {}", 
                    response.messageId(), payload.getUserId());

            return true;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize moderation payload for user {}: {}", 
                    payload.getUserId(), e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send message to moderation queue for user {}: {}", 
                    payload.getUserId(), e.getMessage());
        }

        return false;
    }

    /**
     * Send notification message
     */
    // public boolean sendToNotificationQueue(String messageBody) {
    //     if (notificationQueueUrl == null || notificationQueueUrl.isEmpty()) {
    //         log.warn("Notification queue URL not configured");
    //         return false;
    //     }

    //     try {
    //         SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
    //                 .queueUrl(notificationQueueUrl)
    //                 .messageBody(messageBody)
    //                 .build();

    //         SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            
    //         log.info("Notification message sent. Message ID: {}", response.messageId());
    //         return true;

    //     } catch (Exception e) {
    //         log.error("Failed to send notification message: {}", e.getMessage());
    //         return false;
    //     }
    // }

    /**
     * Receive messages from moderation queue
     */
    public List<Message> receiveModerationMessages() {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(moderationQueueUrl)
                    .maxNumberOfMessages(10) // Max messages per request
                    .waitTimeSeconds(20) // Long polling
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
            
            log.info("Received {} messages from moderation queue", response.messages().size());
            return response.messages();

        } catch (Exception e) {
            log.error("Failed to receive messages from moderation queue: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Process a moderation message
     */
    public ModerationPayload processModerationMessage(Message message) {
        try {
            ModerationPayload payload = objectMapper.readValue(message.body(), ModerationPayload.class);
            log.info("Processed moderation message for user: {}", payload.getUserId());
            return payload;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse moderation message: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete message from queue after processing
     */
    public boolean deleteMessage(String queueUrl, String receiptHandle) {
        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();

            sqsClient.deleteMessage(deleteMessageRequest);
            log.info("Message deleted from queue: {}", receiptHandle);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete message from queue: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get queue attributes (message count, etc.)
     */
    public void getQueueStatus() {
        try {
            // implement queue monitoring here
            log.info("Moderation queue URL: {}", moderationQueueUrl);
        } catch (Exception e) {
            log.error("Failed to get queue status: {}", e.getMessage());
        }
    }
}