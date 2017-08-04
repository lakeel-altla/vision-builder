package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tango.mesh.TangoMesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.lakeel.altla.vision.builder.presentation.graphics.shader.ShaderNames;
import com.lakeel.altla.vision.builder.presentation.graphics.shader.ShaderProgramFactory;
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

    private final DepthShader depthShader = new DepthShader();

    private final WireframeShader wireframeShader = new WireframeShader();

    private final MeshList meshList = new MeshList();

    public TangoMeshRenderer() {
        final Matrix4 worldTransform = new Matrix4();
        worldTransform.rotate(Vector3.X, -90);
        depthShader.worldTransform.set(worldTransform);
        wireframeShader.worldTransform.set(worldTransform);
    }

    @Override
    public void dispose() {
        depthShader.dispose();
        wireframeShader.dispose();
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
        depthShader.begin(camera);

        meshList.render(depthShader, GL20.GL_TRIANGLES);

        depthShader.end();
    }

    public void renderWireframe(@NonNull Camera camera) {
        wireframeShader.begin(camera);

        meshList.render(wireframeShader, GL20.GL_LINES);

        wireframeShader.end();
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

        void unbindAttributes();
    }

    private static final class DepthShader implements Shader, Disposable {

        final ShaderProgram program;

        final int aPosition;

        final int uProjViewWorldTrans;

        final Matrix4 worldTransform = new Matrix4();

        final Matrix4 projViewWorld = new Matrix4();

        DepthShader() {
            program = ShaderProgramFactory.create(ShaderNames.DEPTH);

            aPosition = program.getAttributeLocation("a_position");
            uProjViewWorldTrans = program.getUniformLocation("u_projViewWorldTrans");
        }

        @Override
        public void dispose() {
            program.dispose();
        }

        @Override
        public void bindAttributes() {
            program.enableVertexAttribute(aPosition);
            program.setVertexAttribute(aPosition, 3, GL20.GL_FLOAT, false, 0, 0);
        }

        @Override
        public void unbindAttributes() {
            program.disableVertexAttribute(aPosition);
        }

        void begin(@NonNull Camera camera) {
            program.begin();

            projViewWorld.set(camera.combined).mul(worldTransform);
            program.setUniformMatrix(uProjViewWorldTrans, projViewWorld);
        }

        void end() {
            program.end();
        }
    }

    private static final class WireframeShader implements Shader, Disposable {

        final ShaderProgram program;

        final int aPosition;

        final int uProjViewWorldTrans;

        final int uColor;

        final Matrix4 worldTransform = new Matrix4();

        final Matrix4 projViewWorld = new Matrix4();

        final Color color = Color.GREEN;

        WireframeShader() {
            program = ShaderProgramFactory.create(ShaderNames.FILL_COLOR);

            aPosition = program.getAttributeLocation("a_position");
            uProjViewWorldTrans = program.getUniformLocation("u_projViewWorldTrans");
            uColor = program.getUniformLocation("u_color");
        }

        @Override
        public void dispose() {
            program.dispose();
        }

        @Override
        public void bindAttributes() {
            program.enableVertexAttribute(aPosition);
            program.setVertexAttribute(aPosition, 3, GL20.GL_FLOAT, false, 0, 0);
        }

        @Override
        public void unbindAttributes() {
            program.disableVertexAttribute(aPosition);
        }

        void begin(@NonNull Camera camera) {
            program.begin();

            projViewWorld.set(camera.combined).mul(worldTransform);
            program.setUniformMatrix(uProjViewWorldTrans, projViewWorld);
            program.setUniformf(uColor, color);
        }

        void end() {
            program.end();
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

            shader.unbindAttributes();

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
