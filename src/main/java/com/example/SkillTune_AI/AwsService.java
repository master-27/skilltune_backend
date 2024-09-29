package com.example.SkillTune_AI;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AwsService {

    @Autowired
    private AmazonS3 s3Client;
    final private String BUCKET_NAME = "skilltune-bucket";


    public String uploadAudioFile(String bucketName, InputStream inputStream, Long contentLength, String contentType) throws AmazonClientException {
        // Generate a unique key name for the file (UUID ensures it's unique)
        String keyName = "audio/" + UUID.randomUUID().toString() + ".wav";

        // Set metadata for the file
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);

        // Create a PutObjectRequest, set the ACL to public-read
        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, inputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        // Upload the file to S3
        s3Client.putObject(request);

        // Return the public URL of the uploaded file
        return s3Client.getUrl(bucketName, keyName).toString();
    }

    public String uploadResumeFile(MultipartFile file) throws IOException {

        String keyName = "Mcq/" + UUID.randomUUID().toString() + ".pdf";
        InputStream InputStream = file.getInputStream();
        long contentLength = file.getSize();
        String contentType = file.getContentType();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);  // Should be "application/pdf" for PDF files

        // Create a PutObjectRequest and set the ACL to public-read (if needed)
        PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, keyName,InputStream, metadata);// Optional: public access

        // Upload the file to S3
        s3Client.putObject(request);



        try {
            s3Client.putObject(request);
            return keyName;
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            return null;
        }
    }

//    public ByteArrayOutputStream downloadFile(String bucketName, String keyName) throws IOException, AmazonClientException {
//        S3Object s3Object = s3Client.getObject(bucketName, keyName);
//        InputStream inputStream = s3Object.getObjectContent();
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        int len;
//        byte[] buffer = new byte[4096];
//        while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
//            outputStream.write(buffer, 0, len);
//        }
//        return outputStream;
//    }
//
//    public List<String> listFiles(String bucketName) throws AmazonClientException {
//        List<String> keys = new ArrayList<>();
//        ObjectListing objectListing = s3Client.listObjects(bucketName);
//        while (true) {
//            List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
//            if (objectSummaries.isEmpty()) {
//                break;
//            }
//            objectSummaries.stream()
//                    .filter(item -> !item.getKey().endsWith("/"))
//                    .map(S3ObjectSummary::getKey)
//                    .forEach(keys::add);
//            objectListing = s3Client.listNextBatchOfObjects(objectListing);
//        }
//        return keys;
//    }
//
//    public void deleteFile(String bucketName, String keyName) throws AmazonClientException {
//        s3Client.deleteObject(bucketName, keyName);
//    }
}
