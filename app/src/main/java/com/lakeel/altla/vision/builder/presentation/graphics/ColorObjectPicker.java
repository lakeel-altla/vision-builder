package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.Pool;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ColorObjectPicker implements Disposable {

    private static final Log LOG = LogFactory.getLog(ColorObjectPicker.class);

    private static final String PICK_FRAGMENT_SHADER_SOURCE =
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "uniform vec4 u_baseColor;\n" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = u_baseColor;\n" +
            "}";

    private static final Attributes ATTRIBUTES = new Attributes();

    private FrameBuffer frameBuffer;

    private final Color pickingColor = new Color();

    private final Array<Renderable> renderables = new Array<>();

    private final Pool<Renderable> renderablePool = new FlushablePool<Renderable>() {
        @Override
        protected Renderable newObject() {
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

    private DefaultShader pickShader;

    private Camera camera;

    private RenderContext renderContext;

    @Override
    public void dispose() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
            frameBuffer = null;
        }
        if (pickShader != null) {
            pickShader.dispose();
            pickShader = null;
        }
    }

    public void init() {
        Renderable renderable = new Renderable();
        renderable.environment = new Environment();
        renderable.material = new Material();
        renderable.meshPart.mesh = new Mesh(true, 4, 4, VertexAttribute.Position());

        pickShader = new DefaultShader(renderable, new DefaultShader.Config() {
            {
                this.fragmentShader = PICK_FRAGMENT_SHADER_SOURCE;
            }
        });
        pickShader.init();
    }

    public void begin(@NonNull Camera camera, @NonNull RenderContext renderContext) {
        this.camera = camera;
        this.renderContext = renderContext;

        final int viewportWidth = Gdx.graphics.getWidth();
        final int viewportHeight = Gdx.graphics.getHeight();

        if (frameBuffer != null &&
            (frameBuffer.getWidth() != viewportWidth || frameBuffer.getHeight() != viewportHeight)) {
            frameBuffer.dispose();
            frameBuffer = null;
        }

        if (frameBuffer == null) {
            LOG.i("Creating the frame buffer: viewportWidth = %d, viewportHeight = %d", viewportWidth, viewportHeight);
            frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, viewportWidth, viewportHeight, true);
        }

        frameBuffer.begin();
    }

    public void end() {
        frameBuffer.end();
    }

    public void render(@NonNull Array<ModelInstance> instances) {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        pickShader.begin(camera, renderContext);

        for (int i = 0; i < instances.size; i++) {
            Color.rgba8888ToColor(pickingColor, i);
            pickShader.program.setUniformf("u_baseColor",
                                           pickingColor.r,
                                           pickingColor.g,
                                           pickingColor.b,
                                           pickingColor.a);

            final int offset = renderables.size;
            final ModelInstance instance = instances.get(i);
            instance.getRenderables(renderables, renderablePool);

            for (int j = offset; j < renderables.size; j++) {
                final Renderable renderable = renderables.get(j);
                renderable.shader = pickShader;
                // Render meshes with the empty attributes to ignore the material (e.g. blending) of models.
                pickShader.render(renderable, ATTRIBUTES);
            }
        }

        pickShader.end();

        renderablePool.clear();
        renderables.clear();
    }

    @Nullable
    public ModelInstance pick(@NonNull Array<ModelInstance> instances, int x, int y) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        buffer.order(ByteOrder.nativeOrder());

        Gdx.gl.glReadPixels(x, frameBuffer.getHeight() - y, 1, 1, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buffer);

        buffer.rewind();

        final int r = buffer.get(0) & 0xff;
        final int g = buffer.get(1) & 0xff;
        final int b = buffer.get(2) & 0xff;
        final int a = buffer.get(3) & 0xff;
        final int index = (r << 24) | (g << 16) | (b << 8) | a;

        LOG.d("Color: rgba = [%d, %d, %d, %d], index = %d", r, g, b, a, index);

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
