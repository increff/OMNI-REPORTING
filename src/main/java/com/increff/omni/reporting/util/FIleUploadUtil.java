package com.increff.omni.reporting.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FIleUploadUtil {

    private Storage storage;
    private final String bucketName;
    private static final int PART_SIZE = 5 * 1024 * 1024; // 5 MB

    public FIleUploadUtil(String bucketName, String credentialFilePath)
            throws IOException {
        this.bucketName = bucketName;
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(credentialFilePath));
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public void create(String filePath, InputStream is) throws IOException {

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath).build();

        Blob blob = storage.create(blobInfo);
        // Get the WriteChannel for the Blob
        WriteChannel writeChannel = blob.writer();

        // Read the file and upload it in parts
        byte[] buffer = new byte[PART_SIZE];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) > 0) {
            // Write the part to the WriteChannel
            writeChannel.write(java.nio.ByteBuffer.wrap(buffer, 0, bytesRead));
        }
        // Close the WriteChannel to complete the upload
        writeChannel.close();
    }

    public InputStream get(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        Blob blob = storage.get(blobId);
        return new ByteArrayInputStream(blob.getContent());
    }
}
