package com.lakeel.altla.vision.builder.presentation.graphics.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

import android.support.annotation.NonNull;

public final class OutlineShader implements Shader {

    private ShaderProgram program;

    private int uViewWorldTrans;

    private int uProjViewWorldTrans;

    private int uNormalMatrix;

    private int uProjTrans;

    private int uOutlineWidth;

    private int uOutlineColor;

    private float outlineWidth = 0.05f;

    private final Color outlineColor = Color.YELLOW;

    private final Matrix4 viewWorld = new Matrix4();

    private final Matrix4 projViewWorld = new Matrix4();

    private final Matrix3 normalMatrix = new Matrix3();

    private Camera camera;

    @Override
    public void init() {
        program = ShaderProgramFactory.create(ShaderNames.OUTLINE);

        uViewWorldTrans = program.getUniformLocation("u_viewWorldTrans");
        uProjViewWorldTrans = program.getUniformLocation("u_projViewWorldTrans");
        uNormalMatrix = program.getUniformLocation("u_normalMatrix");
        uProjTrans = program.getUniformLocation("u_projTrans");
        uOutlineWidth = program.getUniformLocation("u_outlineWidth");
        uOutlineColor = program.getUniformLocation("u_outlineColor");
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

        context.setCullFace(GL20.GL_FRONT);

        program.begin();
        program.setUniformMatrix(uProjTrans, camera.projection);
        program.setUniformf(uOutlineWidth, outlineWidth);
        program.setUniformf(uOutlineColor, outlineColor);
    }

    @Override
    public void render(Renderable renderable) {
        viewWorld.set(camera.view).mul(renderable.worldTransform);
        projViewWorld.set(camera.combined).mul(renderable.worldTransform);
        normalMatrix.set(viewWorld).inv().transpose();

        program.setUniformMatrix(uViewWorldTrans, viewWorld);
        program.setUniformMatrix(uProjViewWorldTrans, projViewWorld);
        program.setUniformMatrix(uNormalMatrix, normalMatrix);

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

    public void setOutlineWidth(float outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    public void setuOutlineColor(@NonNull Color color) {
        outlineColor.set(color);
    }
}
