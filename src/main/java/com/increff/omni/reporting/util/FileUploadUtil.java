package com.increff.omni.reporting.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class FileUploadUtil {

    private Storage storage;
    private final String bucketName;

    public FileUploadUtil(String bucketName, String credentialFilePath)
            throws IOException {
        this.bucketName = bucketName;
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(credentialFilePath));
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public URL getSignedUri(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        Blob blob = storage.get(blobId);
        return blob.signUrl(10, TimeUnit.SECONDS);
    }
}
