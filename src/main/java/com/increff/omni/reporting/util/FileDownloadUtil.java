package com.increff.omni.reporting.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class FileDownloadUtil {

    private Storage storage;
    private final String bucketName;

    public FileDownloadUtil(String bucketName, String credentialFilePath)
            throws IOException {
        this.bucketName = bucketName;
//        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(c
//        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public URL getSignedUri(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        return storage.signUrl(blobInfo, 10, TimeUnit.SECONDS);
    }

    public InputStream get(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        Blob blob = storage.get(blobId);
        return new ByteArrayInputStream(blob.getContent());
    }
}
