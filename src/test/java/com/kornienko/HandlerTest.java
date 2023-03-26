package com.kornienko;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HandlerTest {
    private S3Client s3Client;

    @BeforeEach
    void startUp() {
        s3Client = mock(S3Client.class);
    }

    @Test
    void s3ClientCreateBucketMethodIsCalled() {
        when(s3Client.waiter()).thenReturn(S3Waiter.builder().build());
        Handler.createBucket(s3Client, "bucket");
        verify(s3Client).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void s3ClientPutObjectMethodIsCalledAndReturnsResponse() {
        PutObjectResponse response = mock(PutObjectResponse.class);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(response);
        when(response.eTag()).thenReturn("response");
        assertEquals("response", Handler.putS3Object(s3Client, "bucket", "index.html",
                "src/test/resources/website/index.html"));
    }

    @Test
    void getObjectFileAsByteArrayFromFile() {
        String arrayAsString = new String(Handler.getObjectFile("src/test/resources/website/index.html"),
                StandardCharsets.UTF_8);
        assertTrue(arrayAsString.contains("html"));
    }
}
