package com.lakeel.altla.vision.data.repository.android;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public final class AssetCacheRepository {

    private static final Log LOG = LogFactory.getLog(AssetCacheRepository.class);

    private final Context context;

    public AssetCacheRepository(@NonNull Context context) {
        this.context = context;
    }

    @Nullable
    public File find(@NonNull String assetId) {
        final File file = resolveCacheFile(assetId);
        if (file.exists()) {
            LOG.d("The cache file exists: assetId = %s", assetId);
            return file;
        } else {
            return null;
        }
    }

    @NonNull
    public File create(@NonNull String assetId) throws IOException {
        final File file = resolveCacheFile(assetId);
        if (file.createNewFile()) {
            LOG.d("Created the new cache file: assetId = %s", assetId);
        } else {
            LOG.w("The cache file already exists: assetId = %s", assetId);
        }
        return file;
    }

    @NonNull
    public File findOrCreate(@NonNull String assetId) throws IOException {
        File file = find(assetId);
        if (file == null) {
            file = create(assetId);
        }
        return file;
    }

    public void delete(@NonNull String assetId) {
        final File file = resolveCacheFile(assetId);
        if (file.delete()) {
            LOG.d("Deleted the new cache file: assetId = %s", assetId);
        } else {
            LOG.w("The cache file does not exist: assetId = %s", assetId);
        }
    }

    @NonNull
    private File resolveCacheFile(@NonNull String assetId) {
        File directory = new File(context.getCacheDir(), "assets");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return new File(directory, assetId);
    }
}
