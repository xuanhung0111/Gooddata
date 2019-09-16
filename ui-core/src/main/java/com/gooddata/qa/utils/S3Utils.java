package com.gooddata.qa.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.transfer.TransferManager;

public class S3Utils {

    private S3Utils() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    private static Logger logger = LoggerFactory.getLogger(S3Utils.class);

    /**
     * Upload file from local to S3
     *
     * @param localFile       file to upload
     * @param s3FileName      file name in S3
     * @param bucketName
     * @param s3AccessKey
     * @param s3SecretKey
     * @param s3BucketNameUri
     * @throws InterruptedException
     * @throws AmazonClientException
     * @throws AmazonServiceException
     */
    public static void uploadFile(File localFile, String s3FileName, String s3AccessKey, String s3SecretKey,
            String s3BucketNameUri) {
        TransferManager transferManager = null;
        try {
            logger.info(String.format("Uploading '%s' file to S3 '%s' file", localFile.getAbsolutePath(), s3FileName));
            BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
            AmazonS3URI uri = new AmazonS3URI(s3BucketNameUri);
            transferManager = new TransferManager(credentials);
            transferManager.upload(uri.getBucket(), s3FileName, localFile).waitForCompletion();
            logger.info("Upload to S3 completed");
        } catch (Exception e) {
            logger.error("Update file" + localFile.getAbsolutePath() + "failed !!!");
            throw new RuntimeException(e);
        } finally {
            if (transferManager != null) {
                transferManager.shutdownNow();
            }
        }
    }

    /**
     * Delete file from S3
     *
     * @param filePath        file path will be deleted
     * @param bucketName      bucket name in S3
     * @param s3AccessKey     access key access to S3
     * @param s3SecretKey     secret key access to S3
     * @param s3BucketNameUri bucket name URI access to S3
     */
    public static void deleteFile(String filePath, String s3AccessKey, String s3SecretKey,
            String s3BucketNameUri) {
        TransferManager transferManager = null;
        try {
            logger.info(String.format("Delete file'%s' from S3", filePath));
            BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
            AmazonS3URI uri = new AmazonS3URI(s3BucketNameUri);
            transferManager = new TransferManager(credentials);
            transferManager.getAmazonS3Client().deleteObject(uri.getBucket(), filePath);
            logger.info(String.format("Delete file'%s' from S3 completed", filePath));
        } finally {
            if (transferManager != null) {
                transferManager.shutdownNow();
            }
        }
    }
}
