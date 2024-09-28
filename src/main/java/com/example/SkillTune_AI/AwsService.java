package com.example.SkillTune_AI;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public ByteArrayOutputStream downloadFile(String bucketName, String keyName) throws IOException, AmazonClientException {
        S3Object s3Object = s3Client.getObject(bucketName, keyName);
        InputStream inputStream = s3Object.getObjectContent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream;
    }

    public List<String> listFiles(String bucketName) throws AmazonClientException {
        List<String> keys = new ArrayList<>();
        ObjectListing objectListing = s3Client.listObjects(bucketName);
        while (true) {
            List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            if (objectSummaries.isEmpty()) {
                break;
            }
            objectSummaries.stream()
                    .filter(item -> !item.getKey().endsWith("/"))
                    .map(S3ObjectSummary::getKey)
                    .forEach(keys::add);
            objectListing = s3Client.listNextBatchOfObjects(objectListing);
        }
        return keys;
    }

    public void deleteFile(String bucketName, String keyName) throws AmazonClientException {
        s3Client.deleteObject(bucketName, keyName);
    }
}
