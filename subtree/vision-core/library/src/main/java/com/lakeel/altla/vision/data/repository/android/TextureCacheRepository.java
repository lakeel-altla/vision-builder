package com.lakeel.altla.vision.data.repository.android;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public final class TextureCacheRepository {

    private static final Log LOG = LogFactory.getLog(TextureCacheRepository.class);

    private final Context context;

    public TextureCacheRepository(@NonNull Context context) {
        this.context = context;
    }

    @Nullable
    public File find(@NonNull String textureId) {
        File file = resolveCacheFile(textureId);
        if (file.exists()) {
            LOG.d("The cache file exists: textureId = %s", textureId);
            return file;
        } else {
            return null;
        }
    }

    @NonNull
    public File create(@NonNull String textureId) throws IOException {
        File file = resolveCacheFile(textureId);
        if (file.createNewFile()) {
            LOG.d("Created the new cache file: textureId = %s", textureId);
        } else {
            LOG.w("The cache file already exists: textureId = %s", textureId);
        }
        return file;
    }

    public void delete(@NonNull String textureId) {
        File file = resolveCacheFile(textureId);
        if (file.delete()) {
            LOG.d("Deleted the new cache file: textureId = %s", textureId);
        } else {
            LOG.w("The cache file does not exist: textureId = %s", textureId);
        }
    }

    @NonNull
    private File resolveCacheFile(@NonNull String textureId) {
        File directory = new File(context.getCacheDir(), "textures");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return new File(directory, textureId);
    }
}
