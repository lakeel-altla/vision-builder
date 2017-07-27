package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.projecttango.tangosupport.TangoSupport;

import android.opengl.GLES11Ext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Imported from java_opengl_augmented_reality_example in Tango examples
 * and modified by using libgdx classes.
 *
 * A preview of the RGB camera rendered as background using OpenGL.
 */
public final class CameraPreview implements Disposable {

    private static final String VERTEX_SHADER_SOURCE =
            "attribute vec2 a_Position;\n" +
            "attribute vec2 a_TexCoord;\n" +
            "varying vec2 v_TexCoord;\n" +
            "void main() {\n" +
            "  v_TexCoord = a_TexCoord;\n" +
            "  gl_Position = vec4(a_Position.x, a_Position.y, 0.0, 1.0);\n" +
            "}";

    private static final String FRAGMENT_SHADER_SOURCE =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES u_Texture;\n" +
            "varying vec2 v_TexCoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(u_Texture,v_TexCoord);\n" +
            "}";

    private final Mesh mesh;

    private final Texture texture;

    private final ShaderProgram shaderProgram;

    public CameraPreview() {
        mesh = new Mesh();
        texture = new Texture();
        shaderProgram = new ShaderProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
    }

    @Override
    public void dispose() {
        texture.dispose();
        mesh.dispose();
        shaderProgram.dispose();
    }

    public void updateTextureUv(int displayRotation) {
        final float[] texCoords =
                TangoSupport.getVideoOverlayUVBasedOnDisplayRotation(Mesh.TEX_COORDS, displayRotation);
        mesh.setTexCoords(texCoords);
    }

    public void render() {
        shaderProgram.begin();

        final int positionHandle = shaderProgram.getAttributeLocation("a_Position");
        final int texCoordHandle = shaderProgram.getAttributeLocation("a_TexCoord");
        final int textureHandle = shaderProgram.getUniformLocation("u_Texture");

        texture.bind();
        Gdx.gl.glUniform1i(textureHandle, 0);

        mesh.draw(positionHandle, texCoordHandle);

        shaderProgram.end();
    }

    public int getTextureId() {
        return texture.handle;
    }

    private static final class Texture implements Disposable {

        private final int handle;

        Texture() {
            handle = Gdx.gl.glGenTexture();
            Gdx.gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, handle);
            Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
            Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
            Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
            Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
        }

        @Override
        public void dispose() {
            Gdx.gl.glDeleteTexture(handle);
        }

        void bind() {
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            Gdx.gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, handle);
        }
    }

    /**
     * Imported from java_opengl_augmented_reality_example in Tango examples,
     * and modified by using libgdx classes.
     *
     * Mesh class that knows how to generate its VBOs and indices to be drawn in OpenGL.
     */
    private static final class Mesh implements Disposable {

        private static final float[] POSITIONS = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };

        private static final float[] TEX_COORDS = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

        private static final short[] INDICES = { 0, 2, 1, 3 };

        private static final int NUM_VERTEX_COORD = 2;

        private static final int NUM_TEX_COORD = 2;

        private static final int NUM_VERTICES = POSITIONS.length / NUM_VERTEX_COORD;

        private static final int NUM_INDICES = INDICES.length;

        private final FloatBuffer positions;

        private final FloatBuffer texCoords;

        private final ShortBuffer indices;

        private final int vertexBuffer;

        private final int texCoordBuffer;

        private final int indexBuffer;

        /**
         * Create a mesh.
         */
        Mesh() {
            positions = ByteBuffer.allocateDirect(Float.SIZE / 8 * POSITIONS.length)
                                  .order(ByteOrder.nativeOrder())
                                  .asFloatBuffer();
            positions.put(POSITIONS);
            positions.position(0);

            texCoords = ByteBuffer.allocateDirect(Float.SIZE / 8 * TEX_COORDS.length)
                                  .order(ByteOrder.nativeOrder())
                                  .asFloatBuffer();
            texCoords.put(TEX_COORDS);
            texCoords.position(0);


            indices = ByteBuffer.allocateDirect(Short.SIZE / 8 * INDICES.length)
                                .order(ByteOrder.nativeOrder())
                                .asShortBuffer();
            indices.put(INDICES);
            indices.position(0);

            // Generate 3 buffers. Vertex buffer, texture buffer and index buffer.
            vertexBuffer = Gdx.gl.glGenBuffer();
            texCoordBuffer = Gdx.gl.glGenBuffer();
            indexBuffer = Gdx.gl.glGenBuffer();

            // Bind to vertex buffer
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexBuffer);
            // Populate it.
            Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, NUM_VERTICES * NUM_VERTEX_COORD * Float.SIZE / 8, positions,
                                GL20.GL_STATIC_DRAW);

            // Bind to texcoord buffer
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, texCoordBuffer);
            // Populate it.
            Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, NUM_VERTICES * NUM_TEX_COORD * Float.SIZE / 8, texCoords,
                                GL20.GL_STATIC_DRAW);

            // Bind to indices buffer
            Gdx.gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
            // Populate it.
            Gdx.gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, NUM_INDICES * Short.SIZE / 8, indices,
                                GL20.GL_STATIC_DRAW);

            // Unbind buffer.
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
            Gdx.gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        @Override
        public void dispose() {
            Gdx.gl.glDeleteBuffer(vertexBuffer);
            Gdx.gl.glDeleteBuffer(texCoordBuffer);
            Gdx.gl.glDeleteBuffer(indexBuffer);
        }

        void draw(int positionHandle, int texCoordHandle) {
            Gdx.gl.glEnableVertexAttribArray(positionHandle);
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexBuffer);
            Gdx.gl.glVertexAttribPointer(positionHandle, NUM_VERTEX_COORD, GL20.GL_FLOAT, false,
                                         Float.SIZE / 8 * NUM_VERTEX_COORD, 0);

            Gdx.gl.glEnableVertexAttribArray(texCoordHandle);
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, texCoordBuffer);
            Gdx.gl.glVertexAttribPointer(texCoordHandle, NUM_TEX_COORD, GL20.GL_FLOAT, false,
                                         Float.SIZE / 8 * NUM_TEX_COORD, 0);

            Gdx.gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);

            Gdx.gl.glDrawElements(GL20.GL_TRIANGLE_STRIP, NUM_INDICES, GL20.GL_UNSIGNED_SHORT, 0);

            // Unbind.
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
            Gdx.gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        void setTexCoords(float[] texCoords) {
            this.texCoords.put(texCoords);
            this.texCoords.position(0);

            // Bind to texcoord buffer
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, texCoordBuffer);
            // Populate it.
            Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, NUM_VERTICES * NUM_TEX_COORD * Float.SIZE / 8, this.texCoords,
                                GL20.GL_STATIC_DRAW);
        }
    }
}
