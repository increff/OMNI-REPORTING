package com.increff.omni.reporting.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.nextscm.commons.fileclient.FileClientException;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public void create(String filePath, InputStream is, String filename) throws FileClientException {
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo =
                BlobInfo.newBuilder(blobId).setContentDisposition("inline; filename=\"" + filename + "\"").build();

        try {
            storage.create(blobInfo, IOUtils.toByteArray(is));
        } catch (IOException e) {
            throw new FileClientException("Error while creating file:" + e.getMessage(), e);
        }
    }

    public URL getSignedUri(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        return storage.signUrl(blobInfo, 10, TimeUnit.SECONDS);
    }
}
