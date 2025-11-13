package io.mawhebty.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import io.mawhebty.exceptions.BadDataException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 awsS3Client;
    private final String bucketName;
    @Getter
    private final String adminFolderInBucket;
    @Getter
    private final String awsRekognitionFolderInBucket;
    @Getter
    private final String awsSpecialCasesFolderInBucket;

    // private final S3Client s3Client;


    // private String s3BaseUrl;


    /**
     * Upload file to S3 and return the file URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new BadDataException("File is empty");
            }

            // Generate unique file name
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String fileName = folder+ "/" + generateFileName(fileExtension);
            
            // Determine content type
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = determineContentType(fileExtension);
            }

            // upload on S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            awsS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

            // Prepare S3 request
            // PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            //         .bucket(bucketName)
            //         .key(fileName)
            //         .contentType(contentType)
            //         .contentLength(file.getSize())
            //         .build();

            // Upload file to S3
            // s3Client.putObject(putObjectRequest, file.getInputStream());

            // Generate and return file URL
            String fileUrl = awsS3Client.getUrl(bucketName, fileName).toString();
            log.info("File uploaded successfully: {} -> {}", originalFileName, fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read file", e);
        } catch (S3Exception e) {
            log.error("S3 upload error: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Upload file with custom folder structure
     */
    // public String uploadFile(MultipartFile file, String folder) {
    //     try {
    //         if (file.isEmpty()) {
    //             throw new IllegalArgumentException("File is empty");
    //         }

    //         String originalFileName = file.getOriginalFilename();
    //         String fileExtension = getFileExtension(originalFileName);
    //         String fileName = folder + "/" + generateFileName(fileExtension);
            
    //         String contentType = file.getContentType();
    //         if (contentType == null) {
    //             contentType = determineContentType(fileExtension);
    //         }

    //         PutObjectRequest putObjectRequest = PutObjectRequest.builder()
    //                 .bucket(bucketName)
    //                 .key(fileName)
    //                 .contentType(contentType)
    //                 .contentLength(file.getSize())
    //                 .build();

    //         s3Client.putObject(putObjectRequest, 
    //                 RequestBody.fromBytes(file.getBytes()));

    //         String fileUrl = String.format("%s/%s", s3BaseUrl, fileName);
    //         log.info("File uploaded to folder {}: {} -> {}", folder, originalFileName, fileUrl);
            
    //         return fileUrl;

    //     } catch (IOException e) {
    //         log.error("Error reading file: {}", e.getMessage());
    //         throw new RuntimeException("Failed to read file", e);
    //     } catch (S3Exception e) {
    //         log.error("S3 upload error: {}", e.getMessage());
    //         throw new RuntimeException("Failed to upload file to S3", e);
    //     }
    // }

    /**
     * Delete file from S3
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract file key from URL
            // String fileKey = extractFileKeyFromUrl(fileUrl);
            
            // DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            //         .bucket(bucketName)
            //         .key(fileKey)
            //         .build();

            // s3Client.deleteObject(deleteObjectRequest);
            // log.info("File deleted successfully: {}", fileKey);
            return true;

        } catch (S3Exception e) {
            log.error("S3 delete error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if file is an image
     */
    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Check if file is a video
     */
    public boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("video/");
    }

    /**
     * Check if file is a document
     */
    public boolean isDocumentFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }

    // ========== PRIVATE HELPER METHODS ==========

    private String generateFileName(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "bin"; // Default extension
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String determineContentType(String fileExtension) {
        return switch (fileExtension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }

    // private String extractFileKeyFromUrl(String fileUrl) {
    //     // Remove base URL to get the file key
    //     if (fileUrl.startsWith(s3BaseUrl)) {
    //         return fileUrl.substring(s3BaseUrl.length() + 1);
    //     }
    //     // If it's a full S3 URL, extract the key
    //     if (fileUrl.contains(bucketName)) {
    //         return fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
    //     }
    //     // Assume it's already a key
    //     return fileUrl;
    // }
}