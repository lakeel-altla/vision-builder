package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tango.mesh.TangoMesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.lakeel.altla.vision.builder.presentation.helper.GridIndex;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.util.LruCache;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static com.badlogic.gdx.Gdx.gl;

public final class TangoMeshRenderer implements Disposable {

    private final Matrix4 worldTransform = new Matrix4();

    private final Matrix4 mvp = new Matrix4();

    private final DepthShader depthShader = new DepthShader();

    private final ColorShader colorShader = new ColorShader();

    private final MeshList meshList = new MeshList();

    public TangoMeshRenderer() {
        worldTransform.rotate(Vector3.X, -90);
    }

    @Override
    public void dispose() {
        depthShader.dispose();
        colorShader.dispose();
        meshList.dispose();
    }

    public void update(@NonNull List<TangoMesh> tangoMeshes) {
        for (final TangoMesh tangoMesh : tangoMeshes) {
            update(tangoMesh);
        }
    }

    public void update(@NonNull TangoMesh tangoMesh) {
        final GridIndex gridIndex = new GridIndex(tangoMesh.index);
        final Mesh mesh = meshList.get(gridIndex);
        mesh.update(tangoMesh);
    }

    public void renderDepth(@NonNull Camera camera) {
        mvp.set(camera.combined).mul(worldTransform);

        depthShader.program.begin();
        depthShader.bindMvp(mvp);

        meshList.render(depthShader, GL20.GL_TRIANGLES);

        depthShader.program.end();
    }

    public void renderWireframe(@NonNull Camera camera) {
        mvp.set(camera.combined).mul(worldTransform);

        colorShader.program.begin();
        colorShader.bindMvp(mvp);
        colorShader.bindColor(Color.GREEN);

        meshList.render(colorShader, GL20.GL_LINES);

        colorShader.program.end();
    }

    private static final class VertexBuffer implements Disposable {

        static final int SIZE_OF_FLOAT = 4;

        final int handle;

        VertexBuffer() {
            handle = gl.glGenBuffer();
        }

        @Override
        public void dispose() {
            gl.glDeleteBuffer(handle);
        }

        void bind() {
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
        }

        void unbind() {
            gl.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }

        void update(@NonNull FloatBuffer vertices) {
            bind();
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, vertices.capacity() * SIZE_OF_FLOAT, vertices, GL20.GL_DYNAMIC_DRAW);
            unbind();
        }
    }

    private static final class IndexBuffer implements Disposable {

        static final int SIZE_OF_INT = 4;

        final int handle;

        IndexBuffer() {
            handle = gl.glGenBuffer();
        }

        @Override
        public void dispose() {
            gl.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            gl.glDeleteBuffer(handle);
        }

        void bind() {
            gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, handle);
        }

        void unbind() {
            gl.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        void update(@NonNull IntBuffer faces) {
            bind();
            gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, faces.capacity() * SIZE_OF_INT, faces, GL20.GL_DYNAMIC_DRAW);
            unbind();
        }
    }

    private interface Shader {

        void bindAttributes();
    }

    private static final class DepthShader implements Shader, Disposable {

        static final String VERTEX_SHADER_SOURCE =
                "attribute vec4 a_Position;\n" +
                "uniform mat4 u_MVP;\n" +
                "varying vec4 v_Position;\n" +
                "void main() {\n" +
                "  gl_Position = u_MVP * a_Position;\n" +
                "  v_Position = a_Position;\n" +
                "}\n";

        static final String FRAGMENT_SHADER_SOURCE =
                "precision mediump float;\n" +
                "varying vec4 v_Position;\n" +
                "void main() {\n" +
                "  float depth = v_Position.z/v_Position.w;\n" +
                "  gl_FragColor = vec4(depth,depth,depth,1);\n" +
                "}\n";

        final ShaderProgram program;

        DepthShader() {
            program = new ShaderProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        }

        @Override
        public void dispose() {
            program.dispose();
        }

        @Override
        public void bindAttributes() {
            final int location = program.getAttributeLocation("a_Position");
            program.enableVertexAttribute(location);
            program.setVertexAttribute(location, 3, GL20.GL_FLOAT, false, 0, 0);
        }

        void bindMvp(@NonNull Matrix4 mvp) {
            final int location = program.getUniformLocation("u_MVP");
            program.setUniformMatrix(location, mvp);
        }
    }

    private static final class ColorShader implements Shader, Disposable {

        static final String VERTEX_SHADER_SOURCE =
                "attribute vec4 a_Position;\n" +
                "uniform mat4 u_MVP;\n" +
                "void main() {\n" +
                "  gl_Position = u_MVP * a_Position;\n" +
                "}\n";

        static final String FRAGMENT_SHADER_SOURCE =
                "precision mediump float;\n" +
                "uniform vec4 u_Color;\n" +
                "void main() {\n" +
                "  gl_FragColor = u_Color;\n" +
                "}\n";

        final ShaderProgram program;

        ColorShader() {
            program = new ShaderProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        }

        @Override
        public void dispose() {
            program.dispose();
        }

        @Override
        public void bindAttributes() {
            final int location = program.getAttributeLocation("a_Position");
            program.enableVertexAttribute(location);
            program.setVertexAttribute(location, 3, GL20.GL_FLOAT, false, 0, 0);
        }

        void bindMvp(@NonNull Matrix4 mvp) {
            final int location = program.getUniformLocation("u_MVP");
            program.setUniformMatrix(location, mvp);
        }

        void bindColor(@NonNull Color color) {
            final int location = program.getUniformLocation("u_Color");
            program.setUniformf(location, color.r, color.g, color.b, color.a);
        }
    }

    private static final class Mesh implements Disposable {

        final VertexBuffer vertexBuffer = new VertexBuffer();

        final IndexBuffer indexBuffer = new IndexBuffer();

        int numFaces;

        @Override
        public void dispose() {
            vertexBuffer.dispose();
            indexBuffer.dispose();
        }

        void update(@NonNull TangoMesh tangoMesh) {
            vertexBuffer.update(tangoMesh.vertices);
            indexBuffer.update(tangoMesh.faces);
            numFaces = tangoMesh.numFaces;
        }

        void render(@NonNull Shader shader, int primitiveType) {
            vertexBuffer.bind();
            indexBuffer.bind();

            shader.bindAttributes();

            gl.glDrawElements(primitiveType, numFaces * 3, GL20.GL_UNSIGNED_INT, 0);

            vertexBuffer.unbind();
            indexBuffer.unbind();
        }
    }

    private static final class MeshList implements Disposable {

        private static final int CACHE_SIZE = 200;

        final SimpleArrayMap<GridIndex, Mesh> meshMap = new SimpleArrayMap<>();

        final MeshCache cache = new MeshCache(CACHE_SIZE);

        @NonNull
        Mesh get(@NonNull GridIndex gridIndex) {
            Mesh mesh = cache.get(gridIndex);
            if (mesh == null) {
                mesh = new Mesh();
                cache.put(gridIndex, mesh);
                meshMap.put(gridIndex, mesh);
            }
            return mesh;
        }

        @Override
        public void dispose() {
            cache.evictAll();
        }

        void render(@NonNull Shader shader, int primitiveType) {
            for (int i = 0; i < meshMap.size(); i++) {
                meshMap.valueAt(i).render(shader, primitiveType);
            }
        }

        private final class MeshCache extends LruCache<GridIndex, Mesh> {

            MeshCache(int maxSize) {
                super(maxSize);
            }

            @Override
            protected int sizeOf(GridIndex key, Mesh value) {
                return 1;
            }

            @Override
            protected void entryRemoved(boolean evicted, GridIndex key, Mesh oldValue, Mesh newValue) {
                meshMap.remove(key);
                oldValue.dispose();
            }
        }
    }
}
