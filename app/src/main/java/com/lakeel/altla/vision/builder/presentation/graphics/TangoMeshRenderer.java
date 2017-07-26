package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tango.mesh.TangoMesh;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
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

public final class TangoMeshRenderer implements Disposable {

    private static final int NUM_VERTEX_COORD = 3;

    private static final int MESH_CACHE_SIZE = 200;

    private static final Matrix4 WORLD_TRANSFORM = new Matrix4();

    private static final Matrix4 MVP = new Matrix4();

    private final Material material = new Material();

    private final MeshList meshList = new MeshList();

    public TangoMeshRenderer() {
        WORLD_TRANSFORM.rotate(Vector3.X, -90);
    }

    @Override
    public void dispose() {
        material.dispose();
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

    public void render(@NonNull Camera camera) {
        MVP.set(camera.combined).mul(WORLD_TRANSFORM);

        material.shaderProgram.begin();

        for (int i = 0; i < meshList.meshMap.size(); i++) {
            meshList.meshMap.valueAt(i).render(MVP);
        }

        material.shaderProgram.end();
    }

    private class VertexBuffer implements Disposable {

        // The service will always pass four-byte floats.
        static final int SIZE_OF_FLOAT = 4;

        final int handle;

        VertexBuffer() {
            handle = Gdx.gl.glGenBuffer();
        }

        @Override
        public void dispose() {
            Gdx.gl.glDeleteBuffer(handle);
        }

        void bind() {
            Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
        }

        void unbind() {
            Gdx.gl.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }

        void update(@NonNull FloatBuffer vertices) {
            bind();
            Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER,
                                vertices.capacity() * SIZE_OF_FLOAT,
                                vertices,
                                GL20.GL_DYNAMIC_DRAW);
            unbind();
        }
    }

    private class IndexBuffer implements Disposable {

        // The service will always pass four-byte integers.
        static final int SIZE_OF_INT = 4;

        int handle;

        IndexBuffer() {
            handle = Gdx.gl.glGenBuffer();
        }

        @Override
        public void dispose() {
            Gdx.gl.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            Gdx.gl.glDeleteBuffer(handle);
        }

        void bind() {
            Gdx.gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, handle);
        }

        void unbind() {
            Gdx.gl.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        void update(@NonNull IntBuffer faces) {
            bind();
            Gdx.gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER,
                                faces.capacity() * SIZE_OF_INT,
                                faces,
                                GL20.GL_DYNAMIC_DRAW);
            unbind();
        }
    }

    private class Material implements Disposable {

        static final String VERTEX_SHADER_SOURCE =
                "attribute vec4 a_Position;\n" +
                "\n" +
                "uniform mat4 u_MVP;\n" +
                "varying vec4 v_Position;\n" +
                "void main() {\n" +
                "  gl_Position = u_MVP * a_Position;\n" +
                "  v_Position = a_Position;\n" +
                "}\n";

        static final String FRAGMENT_SHADER_SOURCE =
                "precision mediump float;\n" +
                "varying vec4 v_Position;\n" +
                "\n" +
                "void main() {\n" +
                "  gl_FragColor = vec4(v_Position.z/v_Position.w,0,0,1);\n" +
                "}\n";

        final ShaderProgram shaderProgram;

        final int positionHandle;

        final int mvpHandle;

        Material() {
            shaderProgram = new ShaderProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
            positionHandle = shaderProgram.getAttributeLocation("a_Position");
            mvpHandle = shaderProgram.getUniformLocation("u_MVP");
        }

        @Override
        public void dispose() {
            shaderProgram.dispose();
        }
    }

    private class Mesh implements Disposable {

        final VertexBuffer vertexBuffer = new VertexBuffer();

        final IndexBuffer indexBuffer = new IndexBuffer();

        int numFaces;

        @Override
        public void dispose() {
            vertexBuffer.dispose();
            indexBuffer.dispose();
        }

        void update(@NonNull final TangoMesh tangoMesh) {
            vertexBuffer.update(tangoMesh.vertices);
            indexBuffer.update(tangoMesh.faces);
            numFaces = tangoMesh.numFaces;
        }

        void render(@NonNull final Matrix4 mvp) {
            vertexBuffer.bind();
            indexBuffer.bind();

            material.shaderProgram.enableVertexAttribute(material.positionHandle);
            material.shaderProgram.setVertexAttribute(material.positionHandle,
                                                      NUM_VERTEX_COORD,
                                                      GL20.GL_FLOAT,
                                                      false,
                                                      0,
                                                      0);
            material.shaderProgram.setUniformMatrix(material.mvpHandle, mvp);

            Gdx.gl.glDrawElements(GL20.GL_TRIANGLES, numFaces * 3, GL20.GL_UNSIGNED_INT, 0);

            vertexBuffer.unbind();
            indexBuffer.unbind();
        }
    }

    private class MeshList {

        final SimpleArrayMap<GridIndex, Mesh> meshMap = new SimpleArrayMap<>();

        final MeshCache cache = new MeshCache();

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

        private class MeshCache extends LruCache<GridIndex, Mesh> {

            MeshCache() {
                super(MESH_CACHE_SIZE);
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
