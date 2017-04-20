package com.lakeel.altla.vision.data.repository.android;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.IOException;

public final class DocumentBitmapRepository {

    private final ContentResolver contentResolver;

    public DocumentBitmapRepository(@NonNull ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @NonNull
    public Bitmap find(@NonNull Uri uri) throws IOException {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try (ParcelFileDescriptor descriptor = contentResolver.openFileDescriptor(uri, "r")) {
            if (descriptor == null) {
                throw new IllegalStateException("Failed to open the file descriptor: uri = " + uri);
            }

            FileDescriptor fileDescriptor = descriptor.getFileDescriptor();
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            if (bitmap != null) {
                return bitmap;
            } else {
                throw new IllegalStateException("Failed to decode the file descriptor: uri = " + uri);
            }
        }
    }
}
