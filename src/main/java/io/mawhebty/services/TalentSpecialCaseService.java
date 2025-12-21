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

    @Transactional
    public void createSpecialCase(TalentSpecialCaseRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if (talentSpecialCaseRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException("Special case already exists for this user.");
        }

        // Validate file extension
        if(!s3Service.isDocumentFile(request.getFile())){
            throw new BadDataException("Invalid file format, must be pdf/doc");
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
            // create moderation record for this specialCase
            MediaModerationStatus status= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.PENDING.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("PENDING status not found"));

            MediaModeration savedModeration = mediaModerationRepository.save(MediaModeration.builder()
                    .status(status)
                    .build());

            specialCase.setMediaModeration(savedModeration);
            talentSpecialCaseRepository.save(specialCase);
        }

    }

    public void moderateSpecialCase(ModerateUserRequestDto request){
        // create moderation record for this specialCase
        MediaModerationStatus pendingStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.PENDING.getName())
                .orElseThrow(() -> new ResourceNotFoundException("PENDING status not found"));

        MediaModeration media= mediaModerationRepository.findById(request.getMediaId())
                .orElseThrow(()-> new BadDataException("no moderation found with id: "+ request.getMediaId()));

        if(!pendingStatus.getName().equals(media.getStatus().getName())){
            throw new IllegalStateException("can not "+ request.getDecision() +" "+ request.getFileType() +", media is not pending for moderation.");
        }

        if("approved".equals(request.getDecision())){
            MediaModerationStatus approvedStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.APPROVED.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("APPROVED status not found"));
            media.setStatus(approvedStatus);
        }else if("rejected".equals(request.getDecision()) && request.getReason() != null && !request.getReason().isBlank()){
            MediaModerationStatus rejectedStatus= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.REJECTED.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("REJECTED status not found"));
            media.setStatus(rejectedStatus);
            media.setReason(request.getReason());
        }else {
            throw new BadDataException("unsupported decision: "+ request.getDecision());
        }


        mediaModerationRepository.save(media);
    }
}
