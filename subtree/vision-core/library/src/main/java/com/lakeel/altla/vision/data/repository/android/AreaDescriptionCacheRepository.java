package com.lakeel.altla.vision.data.repository.android;

import android.support.annotation.NonNull;

import java.io.File;

public final class AreaDescriptionCacheRepository {

    private static final String PATH = "areaDescriptions";

    private final File rootDirectory;

    public AreaDescriptionCacheRepository(@NonNull File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public boolean exists(@NonNull String areaDescriptionId) {
        File file = resolveCacheFile(areaDescriptionId);
        return file.exists();
    }

    @NonNull
    public File getDirectory() {
        return ensureCacheDirectory();
    }

    @NonNull
    public File getFile(@NonNull String areaDescriptionId) {
        return resolveCacheFile(areaDescriptionId);
    }

    public void delete(@NonNull String areaDescriptionId) {
        File file = resolveCacheFile(areaDescriptionId);
        if (file.exists()) {
            file.delete();
        }
    }

    @NonNull
    private File ensureCacheDirectory() {
        File directory = new File(rootDirectory, PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    @NonNull
    private File resolveCacheFile(String areaDescriptionId) {
        File directory = ensureCacheDirectory();
        return new File(directory, areaDescriptionId);
    }
}
