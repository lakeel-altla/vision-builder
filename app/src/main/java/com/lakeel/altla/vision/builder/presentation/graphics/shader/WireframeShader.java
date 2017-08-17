package com.lakeel.altla.vision.builder.presentation.graphics.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public final class WireframeShader implements Shader {

    public final Color color = Color.GREEN;

    private ShaderProgram program;

    private int uProjViewWorldTrans;

    private int uColor;

    private final Matrix4 projViewWorld = new Matrix4();

    private Camera camera;

    @Override
    public void init() {
        program = ShaderProgramFactory.create(ShaderNames.FILL_COLOR);

        uProjViewWorldTrans = program.getUniformLocation("u_projViewWorldTrans");
        uColor = program.getUniformLocation("u_color");
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return instance.userData == WireframeShader.class;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;

        program.begin();
    }

    @Override
    public void render(Renderable renderable) {
        projViewWorld.set(camera.combined).mul(renderable.worldTransform);
        program.setUniformMatrix(uProjViewWorldTrans, projViewWorld);
        program.setUniformf(uColor, color);

        // Set GL_LINES into the primitive type of the renderable.
        final int primitiveTypeMemento = renderable.meshPart.primitiveType;
        renderable.meshPart.primitiveType = GL20.GL_LINES;

        renderable.meshPart.render(program, true);

        // Restore the primitive type of the renderable.
        renderable.meshPart.primitiveType = primitiveTypeMemento;
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
