package io.mawhebty.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.mawhebty.exceptions.BadDataException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;
    @Getter
    private final String adminFolderInBucket;
    @Getter
    private final String awsRekognitionFolderInBucket;
    @Getter
    private final String awsSpecialCasesFolderInBucket;

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
            String fileName = folder + "/" + generateFileName(fileExtension);

            // Determine content type
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = determineContentType(fileExtension);
            }

            // Upload to S3 using AWS SDK v2
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Generate file URL
            String fileUrl = generateFileUrl(fileName);
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
     * Delete file from S3
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract file key from URL
            String fileKey = extractFileKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", fileKey);
            return true;

        } catch (S3Exception e) {
            log.error("S3 delete error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate S3 file URL
     */
    private String generateFileUrl(String fileKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                getRegionFromClient(),
                fileKey);
    }

    /**
     * Extract region from S3 client
     */
    private String getRegionFromClient() {
        // You can store region in a field or extract from client
        // For simplicity, we'll return the region string
        // You might need to modify this based on your setup
        return "us-east-1"; // Replace with your actual region or get from client
    }

    /**
     * Extract file key from URL
     */
    private String extractFileKeyFromUrl(String fileUrl) {
        // Handle different URL formats
        String[] patterns = {
                "https://" + bucketName + ".s3.",
                "http://" + bucketName + ".s3.",
                "s3://" + bucketName + "/"
        };

        for (String pattern : patterns) {
            if (fileUrl.contains(pattern)) {
                return fileUrl.substring(fileUrl.indexOf(pattern) + pattern.length());
            }
        }

        // If no pattern matches, assume it's already a key
        return fileUrl;
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
        String fileName = file.getOriginalFilename();

        if (fileName == null) return false;

        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        boolean extCheck = ext.matches("(mp4|mov|avi|mkv|wmv|flv)");
        boolean typeCheck = contentType != null && contentType.startsWith("video/");

        return extCheck || typeCheck;
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
}