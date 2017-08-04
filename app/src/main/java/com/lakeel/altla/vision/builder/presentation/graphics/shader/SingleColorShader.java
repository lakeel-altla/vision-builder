package com.lakeel.altla.vision.builder.presentation.graphics.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class SingleColorShader implements Shader {

    public final Color color = Color.ORANGE;

    private ShaderProgram program;

    private int uProjViewWorldTrans;

    private int uColor;

    private final Matrix4 projViewWorld = new Matrix4();

    private Camera camera;

    @Override
    public void init() {
        if (program == null) {
            program = new ShaderProgram(ShaderSources.getVertexShaderSource(ShaderSources.Names.SINGLE_COLOR),
                                        ShaderSources.getFragmentShaderSource(ShaderSources.Names.SINGLE_COLOR));
            if (!program.isCompiled()) throw new GdxRuntimeException(program.getLog());
        }

        uProjViewWorldTrans = program.getUniformLocation("u_projViewWorldTrans");
        uColor = program.getUniformLocation("u_color");
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;

        program.begin();
        program.setUniformf(uColor, color);
    }

    @Override
    public void render(Renderable renderable) {
        projViewWorld.set(camera.combined).mul(renderable.worldTransform);
        program.setUniformMatrix(uProjViewWorldTrans, projViewWorld);

        renderable.meshPart.render(program, true);
    }

    @Override
    public void end() {
        program.end();

        camera = null;
    }

    @Override
    public void dispose() {
        program.dispose();
    }
}
