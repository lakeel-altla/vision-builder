package com.lakeel.altla.vision.data.repository.android;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

public final class DocumentRepository {

    private final ContentResolver contentResolver;

    public DocumentRepository(@NonNull ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @NonNull
    public InputStream openInputStream(@NonNull Uri uri) throws IOException {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream == null) {
            throw new IOException(String.format("Failed to open the stream: uri = %s", uri));
        }

        return inputStream;
    }

    @NonNull
    public InputStream openInputStream(@NonNull String uriString) throws IOException {
        Uri uri = Uri.parse(uriString);
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream == null) {
            throw new IOException(String.format("Failed to open the stream: uri = %s", uriString));
        }

        return inputStream;
    }
}
