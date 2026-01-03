package io.mawhebty.services;

import io.mawhebty.dtos.requests.InternalServices.ModerateUserRequestDto;
import io.mawhebty.dtos.requests.TalentSpecialCaseRequest;
import io.mawhebty.enums.MediaModerationStatusEnum;
import io.mawhebty.enums.ModerationTypeEnum;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.models.*;
import io.mawhebty.repository.MediaModerationRepository;
import io.mawhebty.repository.MediaModerationStatusRepository;
import io.mawhebty.repository.TalentSpecialCaseRepository;
import io.mawhebty.repository.UserRepository;
import io.mawhebty.support.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TalentSpecialCaseService {
    private final TalentSpecialCaseRepository talentSpecialCaseRepository;
    private final S3Service s3Service;
    private final ModerationQueueService moderationQueueService;
    private final MediaModerationRepository mediaModerationRepository;
    private final MediaModerationStatusRepository mediaModerationStatusRepository;
    private final UserRepository userRepository;
    private final MessageService messageService; // Added

    @Transactional
    public void createSpecialCase(TalentSpecialCaseRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        messageService.getMessage("user.not.found.email.generic")
                ));

        if (talentSpecialCaseRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException(
                    messageService.getMessage("special.case.already.exists")
            );
        }

        // Validate file extension
        if(!s3Service.isDocumentFile(request.getFile())){
            throw new BadDataException(
                    messageService.getMessage("special.case.invalid.file.format")
            );
        }

        //Upload file to S3
        String fileUrl = s3Service.uploadFile(request.getFile(), s3Service.getAwsSpecialCasesFolderInBucket());

        //Save to DB
        TalentSpecialCase specialCase = talentSpecialCaseRepository.save(TalentSpecialCase.builder()
                .user(user)
                .s3FileUrl(fileUrl)
                .shortBrief(request.getShortBrief())
                .build());

        // Send message to SQS for moderation
        boolean messageWasSent= moderationQueueService.sendFileForModeration(
                user.getId(),
                user.getRole().getId(),
                "SPECIAL_CASE_DOCUMENT",
                ModerationTypeEnum.DOCUMENT_VERIFICATION.name(),
                specialCase.getId(),
                fileUrl
        );

        // in case the message successfully delivered to sqs, create moderation record
        if(messageWasSent){
            MediaModerationStatus status= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.PENDING.getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageService.getMessage("media.moderation.status.not.found",
                                    new Object[]{MediaModerationStatusEnum.PENDING.getName()})
                    ));

            MediaModeration savedModeration = mediaModerationRepository.save(MediaModeration.builder()
                    .status(status)
                    .build());

            specialCase.setMediaModeration(savedModeration);
            talentSpecialCaseRepository.save(specialCase);
        }
    }

    public void moderateSpecialCase(ModerateUserRequestDto request){
        MediaModerationStatus pendingStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.PENDING.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageService.getMessage("media.moderation.status.not.found",
                                new Object[]{MediaModerationStatusEnum.PENDING.getName()})
                ));

        MediaModeration media= mediaModerationRepository.findById(request.getMediaId())
                .orElseThrow(() -> new BadDataException(
                        messageService.getMessage("moderation.not.found",
                                new Object[]{request.getMediaId()})
                ));

        if(!pendingStatus.getName().equals(media.getStatus().getName())){
            throw new IllegalStateException(
                    messageService.getMessage("moderation.not.pending",
                            new Object[]{request.getDecision(), request.getFileType()})
            );
        }

        if("approved".equals(request.getDecision())){
            MediaModerationStatus approvedStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.APPROVED.getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageService.getMessage("media.moderation.status.not.found",
                                    new Object[]{MediaModerationStatusEnum.APPROVED.getName()})
                    ));
            media.setStatus(approvedStatus);
        }else if("rejected".equals(request.getDecision()) && request.getReason() != null && !request.getReason().isBlank()){
            MediaModerationStatus rejectedStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.REJECTED.getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageService.getMessage("media.moderation.status.not.found",
                                    new Object[]{MediaModerationStatusEnum.REJECTED.getName()})
                    ));
            media.setStatus(rejectedStatus);
            media.setReason(request.getReason());
        }else {
            throw new BadDataException(
                    messageService.getMessage("unsupported.decision",
                            new Object[]{request.getDecision()})
            );
        }

        mediaModerationRepository.save(media);
    }
}