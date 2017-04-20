package com.lakeel.altla.vision.data.repository.android;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class DocumentFilenameRepository {

    private final ContentResolver contentResolver;

    public DocumentFilenameRepository(@NonNull ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Nullable
    public String find(@NonNull Uri uri) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            throw new IllegalStateException(
                    String.format("Can not get a cursor to resolve the filename: uri = %s", uri));
        }

        int index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(index);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }
}
