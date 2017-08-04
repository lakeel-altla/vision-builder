package com.lakeel.altla.vision.builder.presentation.graphics.shader;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import android.support.annotation.NonNull;

public final class ShaderProgramFactory {

    private ShaderProgramFactory() {
    }

    @NonNull
    public static ShaderProgram create(@NonNull String shaderName) {
        return create(shaderName, shaderName);
    }

    @NonNull
    public static ShaderProgram create(@NonNull String vertexShaderName, @NonNull String fragmentShaderName) {
        final ShaderProgram program = new ShaderProgram(ShaderSources.getVertexShaderSource(vertexShaderName),
                                                        ShaderSources.getFragmentShaderSource(fragmentShaderName));
        if (!program.isCompiled()) throw new GdxRuntimeException(program.getLog());
        return program;
    }
}
