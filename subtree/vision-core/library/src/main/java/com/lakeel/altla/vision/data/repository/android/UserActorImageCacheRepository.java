package com.lakeel.altla.vision.data.repository.android;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public final class UserActorImageCacheRepository {

    private static final Log LOG = LogFactory.getLog(UserActorImageCacheRepository.class);

    private final Context context;

    public UserActorImageCacheRepository(@NonNull Context context) {
        this.context = context;
    }

    @Nullable
    public File find(@NonNull String imageId) {
        File file = resolveCacheFile(imageId);
        if (file.exists()) {
            LOG.d("The cache file exists: imageId = %s", imageId);
            return file;
        } else {
            return null;
        }
    }

    @NonNull
    public File create(@NonNull String imageId) throws IOException {
        File file = resolveCacheFile(imageId);
        if (file.createNewFile()) {
            LOG.d("Created the new cache file: imageId = %s", imageId);
        } else {
            LOG.w("The cache file already exists: imageId = %s", imageId);
        }
        return file;
    }

    public void delete(@NonNull String imageId) {
        File file = resolveCacheFile(imageId);
        if (file.delete()) {
            LOG.d("Deleted the new cache file: imageId = %s", imageId);
        } else {
            LOG.w("The cache file does not exist: imageId = %s", imageId);
        }
    }

    @NonNull
    private File resolveCacheFile(@NonNull String imageId) {
        File directory = new File(context.getCacheDir(), "userActorImages");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return new File(directory, imageId);
    }
}
