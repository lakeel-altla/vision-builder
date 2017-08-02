package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tango.mesh.TangoMesh;

import com.badlogic.gdx.Gdx;
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

public final class TangoMeshRenderer implements Disposable {

    private static final int NUM_VERTEX_COORD = 3;

    private static final int MESH_CACHE_SIZE = 200;

    private final Matrix4 worldTransform = new Matrix4();

    private final Matrix4 mvp = new Matrix4();

    private final DepthMaterial depthMaterial = new DepthMaterial();

    private final ColorMaterial colorMaterial = new ColorMaterial();

    private final MeshList meshList = new MeshList();

    private int primitiveType = GL20.GL_TRIANGLES;

    public TangoMeshRenderer() {
        worldTransform.rotate(Vector3.X, -90);
    }

    @Override
    public void dispose() {
        depthMaterial.dispose();
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

        depthMaterial.shaderProgram.begin();
        depthMaterial.bindMvp(mvp);

        for (int i = 0; i < meshList.meshMap.size(); i++) {
            meshList.meshMap.valueAt(i).render(depthMaterial);
        }

        depthMaterial.shaderProgram.end();
    }

    public void renderWireframe(@NonNull Camera camera) {
        mvp.set(camera.combined).mul(worldTransform);

        colorMaterial.shaderProgram.begin();
        colorMaterial.bindMvp(mvp);
        colorMaterial.bindColor(Color.GREEN);

        primitiveType = GL20.GL_LINES;

        for (int i = 0; i < meshList.meshMap.size(); i++) {
            meshList.meshMap.valueAt(i).render(colorMaterial);
        }

        primitiveType = GL20.GL_TRIANGLES;

        colorMaterial.shaderProgram.end();
    }

    private static class VertexBuffer implements Disposable {

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

    private static class IndexBuffer implements Disposable {

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

    private interface Material {

        void bindAttributes();
    }

    private static class DepthMaterial implements Material, Disposable {

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

        final ShaderProgram shaderProgram;

        final int positionHandle;

        final int mvpHandle;

        DepthMaterial() {
            shaderProgram = new ShaderProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
            positionHandle = shaderProgram.getAttributeLocation("a_Position");
            mvpHandle = shaderProgram.getUniformLocation("u_MVP");
        }

        @Override
        public void dispose() {
            shaderProgram.dispose();
        }

        @Override
        public void bindAttributes() {
            shaderProgram.enableVertexAttribute(positionHandle);
            shaderProgram.setVertexAttribute(positionHandle, NUM_VERTEX_COORD, GL20.GL_FLOAT, false, 0, 0);
        }

        void bindMvp(@NonNull Matrix4 mvp) {
            shaderProgram.setUniformMatrix(mvpHandle, mvp);
        }
    }

    private static class ColorMaterial implements Material, Disposable {

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

        final ShaderProgram shaderProgram;

        final int positionHandle;

        final int mvpHandle;

        final int colorHandle;

        ColorMaterial() {
            shaderProgram = new ShaderProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
            positionHandle = shaderProgram.getAttributeLocation("a_Position");
            mvpHandle = shaderProgram.getUniformLocation("u_MVP");
            colorHandle = shaderProgram.getUniformLocation("u_Color");
        }

        @Override
        public void dispose() {
            shaderProgram.dispose();
        }

        @Override
        public void bindAttributes() {
            shaderProgram.enableVertexAttribute(positionHandle);
            shaderProgram.setVertexAttribute(positionHandle, NUM_VERTEX_COORD, GL20.GL_FLOAT, false, 0, 0);
        }

        void bindMvp(@NonNull Matrix4 mvp) {
            shaderProgram.setUniformMatrix(mvpHandle, mvp);
        }

        void bindColor(@NonNull Color color) {
            shaderProgram.setUniformf(colorHandle, color.r, color.g, color.b, color.a);
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

        void update(@NonNull TangoMesh tangoMesh) {
            vertexBuffer.update(tangoMesh.vertices);
            indexBuffer.update(tangoMesh.faces);
            numFaces = tangoMesh.numFaces;
        }

        void render(@NonNull Material material) {
            vertexBuffer.bind();
            indexBuffer.bind();

            material.bindAttributes();
            Gdx.gl.glDrawElements(primitiveType, numFaces * 3, GL20.GL_UNSIGNED_INT, 0);

            vertexBuffer.unbind();
            indexBuffer.unbind();
        }
    }

    private class MeshList implements Disposable {

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

        @Override
        public void dispose() {
            for (int i = 0; i < meshMap.size(); i++) {
                meshMap.valueAt(i).dispose();
            }
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
