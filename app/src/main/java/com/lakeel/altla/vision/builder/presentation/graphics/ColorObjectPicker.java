package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FlushablePool;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.presentation.graphics.shader.FillColorShader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ColorObjectPicker implements Disposable {

    private static final Log LOG = LogFactory.getLog(ColorObjectPicker.class);

    private final Color color = new Color();

    private final Array<Renderable> renderables = new Array<>();

    private final FlushablePool<Renderable> renderablePool = new FlushablePool<Renderable>() {
        @Override
        protected Renderable newObject() {
            LOG.v("Creating a new Renderable that will be pooled.");
            return new Renderable();
        }

        @Override
        public Renderable obtain() {
            final Renderable renderable = super.obtain();
            renderable.environment = null;
            renderable.material = null;
            renderable.meshPart.set("", null, 0, 0, 0);
            renderable.shader = null;
            return renderable;
        }
    };

    private final ByteBuffer byteBuffer;

    private FillColorShader shader = new FillColorShader();

    private Camera camera;

    private RenderContext renderContext;

    private FrameBuffer frameBuffer;

    public ColorObjectPicker() {
        byteBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
        shader.init();
    }

    @Override
    public void dispose() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
            frameBuffer = null;
        }
        if (shader != null) {
            shader.dispose();
            shader = null;
        }
    }

    public void resize(int width, int height) {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
    }

    public void begin(@NonNull Camera camera, @NonNull RenderContext renderContext) {
        this.camera = camera;
        this.renderContext = renderContext;
        frameBuffer.begin();
    }

    public void end() {
        frameBuffer.end();
    }

    public void render(@NonNull final Array<ModelInstance> instances) {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shader.begin(camera, renderContext);

        // Disable the blending.
        renderContext.setBlending(false, 0, 0);

        for (int i = 0; i < instances.size; i++) {
            Color.rgba8888ToColor(color, i);
            shader.color.set(color);

            final int offset = renderables.size;
            final ModelInstance instance = instances.get(i);
            instance.getRenderables(renderables, renderablePool);

            for (int j = offset; j < renderables.size; j++) {
                final Renderable renderable = renderables.get(j);

                // Set the culling mode if needed.
                final IntAttribute cullFace = (IntAttribute) renderable.material.get(IntAttribute.CullFace);
                if (cullFace != null) {
                    renderContext.setCullFace(cullFace.value);
                }

                shader.render(renderable);
            }
        }

        shader.end();

        renderablePool.flush();
        renderables.clear();
    }

    @Nullable
    public ModelInstance pick(@NonNull Array<ModelInstance> instances, int x, int y) {
        Gdx.gl.glReadPixels(x, frameBuffer.getHeight() - y, 1, 1, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, byteBuffer);

        byteBuffer.rewind();

        final int r = byteBuffer.get(0) & 0xff;
        final int g = byteBuffer.get(1) & 0xff;
        final int b = byteBuffer.get(2) & 0xff;
        final int a = byteBuffer.get(3) & 0xff;
        final int index = (r << 24) | (g << 16) | (b << 8) | a;

        if (0 <= index && index < instances.size) {
            return instances.get(index);
        } else {
            return null;
        }
    }

    @Nullable
    public Texture getColorBufferTexture() {
        if (frameBuffer != null) {
            return frameBuffer.getColorBufferTexture();
        } else {
            return null;
        }
    }
}
