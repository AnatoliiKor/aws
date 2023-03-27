package com.kornienko;

import org.junit.jupiter.api.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import static com.kornienko.Handler.createBucket;
import static com.kornienko.Handler.putS3Object;
import static org.junit.jupiter.api.Assertions.*;

@Tag("IntegrationTest")
@Disabled
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {
    private S3Client s3Client;
    private static final String bucketName = "anatolii-kornienko-test-bucket";
    private static final String key = "index.html";

    @BeforeEach
    void initiateS3Client() {
        s3Client = DependencyFactory.s3Client();
    }

    @Test
    @Order(1)
    void createBucketTest() {
        createBucket(s3Client, bucketName);
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
        assertNotNull(s3Client.headBucket(headBucketRequest));
        s3Client.close();
    }

    @Test
    @Order(2)
    void putS3ObjectTest() throws IOException {
        putS3Object(s3Client, bucketName, key, "src/test/resources/website/" + key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s3Client.getObject(getObjectRequest)));

        String line;
        StringBuilder objectFromS3 = new StringBuilder();
        while ((line=reader.readLine()) != null) {
            objectFromS3.append(line);
        }
        assertTrue(objectFromS3.toString().contains("html"));
        s3Client.close();
    }

    @Test
    @Order(3)
    void cleanUpTest() {
        Handler.cleanUp(s3Client, bucketName, Collections.singletonList(key));
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
        assertThrows(NoSuchBucketException.class, () -> s3Client.headBucket(headBucketRequest));
        s3Client.close();
    }

}
