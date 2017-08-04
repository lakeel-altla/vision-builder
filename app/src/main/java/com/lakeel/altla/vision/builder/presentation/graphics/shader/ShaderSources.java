package com.lakeel.altla.vision.builder.presentation.graphics.shader;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import static com.badlogic.gdx.Gdx.files;

public final class ShaderSources {

    private static final String INTERNAL_SHADERS_PATH = "shaders/";

    private static final String VERTEX_SHADER_SOURCE_FILE_SUFFIX = ".vertex.glsl";

    private static final String FRAGMENT_SHADER_SOURCE_FILE_SUFFIX = ".fragment.glsl";

    private static SimpleArrayMap<String, String> shaderSourceMap = new SimpleArrayMap<>();

    private ShaderSources() {
    }

    @NonNull
    public static String getVertexShaderSource(@NonNull String name) {
        return getShaderSource(resolveVertexShaderSourceFilename(name));
    }

    @NonNull
    public static String getFragmentShaderSource(@NonNull String name) {
        return getShaderSource(resolveFragmentShaderSourceFilename(name));
    }

    @NonNull
    private static String getShaderSource(@NonNull String filename) {
        String source = shaderSourceMap.get(filename);
        if (source == null) {
            source = readShaderSource(filename);
            shaderSourceMap.put(filename, source);
        }
        return source;
    }

    @NonNull
    private static String readShaderSource(@NonNull String filename) {
        return files.internal(INTERNAL_SHADERS_PATH + filename).readString();
    }

    @NonNull
    private static String resolveVertexShaderSourceFilename(@NonNull String name) {
        return name + VERTEX_SHADER_SOURCE_FILE_SUFFIX;
    }

    @NonNull
    private static String resolveFragmentShaderSourceFilename(@NonNull String name) {
        return name + FRAGMENT_SHADER_SOURCE_FILE_SUFFIX;
    }
}
