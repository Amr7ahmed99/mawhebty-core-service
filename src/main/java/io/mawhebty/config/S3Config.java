package io.mawhebty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.bucket.admin.folder-name}")
    private String adminFolderInBucket;

    @Value("${aws.s3.bucket.rekognition.folder-name}")
    private String awsRekognitionFolderInBucket;

    @Value("${aws.s3.bucket.talent.special.case.folder-name}")
    private String awsSpecialCasesFolderInBucket;


    @Bean
    public AmazonS3 s3Client() {
        // AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
                // return S3Client.builder()
        //         .region(Region.of(region))
        //         .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        //         .build();
        AWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(region)
                .build();
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }

    @Bean
    public String adminFolderInBucket() {
        return adminFolderInBucket;
    }

    @Bean
    public String awsRekognitionFolderInBucket() {
        return awsRekognitionFolderInBucket;
    }

    @Bean
    public String awsSpecialCasesFolderInBucket() {
        return awsSpecialCasesFolderInBucket;
    }
}