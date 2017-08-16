package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.lakeel.altla.vision.model.BoxCollisionComponent;
import com.lakeel.altla.vision.model.BoxMeshComponent;
import com.lakeel.altla.vision.model.Component;
import com.lakeel.altla.vision.model.SphereCollisionComponent;
import com.lakeel.altla.vision.model.SphereMeshComponent;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;

public final class PrimitiveModelFactory implements Disposable {

    private static final int SHAPE_BOX = 0;

    private static final int SHAPE_SPHERE = 1;

    private final SparseArrayCompat<Builder> builderMap = new SparseArrayCompat<>();

    private final SparseArrayCompat<Model> modelMap = new SparseArrayCompat<>();

    public PrimitiveModelFactory() {
        builderMap.put(SHAPE_BOX, new BoxModelBuilder());
        builderMap.put(SHAPE_SPHERE, new SphereModelBuilder());
    }

    @Override
    public void dispose() {
        for (int i = 0; i < modelMap.size(); i++) {
            modelMap.valueAt(i).dispose();
        }
        modelMap.clear();
    }

    @NonNull
    public Model create(@NonNull Class<? extends Component> clazz) {

        final int shapeId;
        if (BoxCollisionComponent.class == clazz || BoxMeshComponent.class == clazz) {
            shapeId = SHAPE_BOX;
        } else if (SphereCollisionComponent.class == clazz || SphereMeshComponent.class == clazz) {
            shapeId = SHAPE_SPHERE;
        } else {
            throw new IllegalArgumentException("'clazz' is invalid: " + clazz);
        }

        Model model = modelMap.get(shapeId);
        if (model == null) {
            final Builder builder = builderMap.get(shapeId);
            if (builder == null) throw new IllegalArgumentException("'shapeId' is invalid: " + shapeId);

            model = builder.build();

            modelMap.put(shapeId, model);
        }

        return model;
    }

    private interface Builder {

        Model build();
    }

    private static final class BoxModelBuilder implements Builder {

        private static final float SIZE = 0.5f;

        private static final float HALF_SIZE = SIZE * 0.5f;

        private static final Vector3[] NORMALS = {
                new Vector3(0, 1, 0),
                new Vector3(0, -1, 0),
                new Vector3(0, 0, -1),
                new Vector3(0, 0, 1),
                new Vector3(-1, 0, 0),
                new Vector3(1, 0, 0)
        };

        private final ModelBuilder modelBuilder = new ModelBuilder();

        private final Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));

        private final Vector3 side1 = new Vector3();

        private final Vector3 side2 = new Vector3();

        private final MeshPartBuilder.VertexInfo vertexInfo = new MeshPartBuilder.VertexInfo();

        private final Vector3 position = new Vector3();

        @Override
        @NonNull
        public Model build() {
            modelBuilder.begin();

            for (int i = 0; i < NORMALS.length; i++) {
                final MeshPartBuilder meshPartBuilder = modelBuilder.part("face_" + i,
                                                                          GL20.GL_TRIANGLES,
                                                                          Position | Normal,
                                                                          material);

                final Vector3 normal = NORMALS[i];
                side1.set(normal.y, normal.z, normal.x);
                side2.set(side1).crs(normal);

                vertexInfo.set(position.set(normal).sub(side1).sub(side2).scl(HALF_SIZE), normal, null, null);
                final short index0 = meshPartBuilder.vertex(vertexInfo);

                vertexInfo.set(position.set(normal).sub(side1).add(side2).scl(HALF_SIZE), normal, null, null);
                final short index1 = meshPartBuilder.vertex(vertexInfo);

                vertexInfo.set(position.set(normal).add(side1).add(side2).scl(HALF_SIZE), normal, null, null);
                final short index2 = meshPartBuilder.vertex(vertexInfo);

                vertexInfo.set(position.set(normal).add(side1).sub(side2).scl(HALF_SIZE), normal, null, null);
                final short index3 = meshPartBuilder.vertex(vertexInfo);

                meshPartBuilder.index(index0);
                meshPartBuilder.index(index1);
                meshPartBuilder.index(index2);
                meshPartBuilder.index(index0);
                meshPartBuilder.index(index2);
                meshPartBuilder.index(index3);
            }

            return modelBuilder.end();
        }
    }

    private static final class SphereModelBuilder implements Builder {

        private static final float DIAMETER = 0.5f;

        private final ModelBuilder modelBuilder = new ModelBuilder();

        private final Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));

        private final Vector3 position = new Vector3();

        private final Vector3 normal = new Vector3();

        private final MeshPartBuilder.VertexInfo vertexInfo = new MeshPartBuilder.VertexInfo();

        @Override
        @NonNull
        public Model build() {
            modelBuilder.begin();

            final MeshPartBuilder meshPartBuilder = modelBuilder.part("face",
                                                                      GL20.GL_TRIANGLES,
                                                                      Position | Normal,
                                                                      material);

            final float radius = DIAMETER * 0.5f;
            final int tessellation = 16;

            final int verticalSegments = tessellation;
            final int horizontalSegments = tessellation * 2;

            int vertexCount = 0;

            normal.set(0, -1, 0);
            position.set(normal).scl(radius);
            meshPartBuilder.vertex(vertexInfo.set(position, normal, null, null));
            vertexCount++;

            for (int i = 0; i < verticalSegments - 1; i++) {
                double latitude = ((i + 1) * Math.PI / verticalSegments) - Math.PI * 0.5d;
                float dy = (float) Math.sin(latitude);
                float dxz = (float) Math.cos(latitude);

                for (int j = 0; j < horizontalSegments; j++) {
                    double longitude = j * Math.PI * 2.0d / horizontalSegments;
                    float dx = (float) Math.cos(longitude) * dxz;
                    float dz = (float) Math.sin(longitude) * dxz;

                    normal.set(dx, dy, dz);
                    position.set(normal).scl(radius);
                    meshPartBuilder.vertex(vertexInfo.set(position, normal, null, null));
                    vertexCount++;
                }
            }

            normal.set(0, 1, 0);
            position.set(normal).scl(radius);
            meshPartBuilder.vertex(vertexInfo.set(position, normal, null, null));
            vertexCount++;

            for (int i = 0; i < horizontalSegments; i++) {
                meshPartBuilder.index((short) 0);
                meshPartBuilder.index((short) (1 + (i + 1) % horizontalSegments));
                meshPartBuilder.index((short) (1 + i));
            }

            for (int i = 0; i < verticalSegments - 2; i++) {
                for (int j = 0; j < horizontalSegments; j++) {
                    int nextI = i + 1;
                    int nextJ = (j + 1) % horizontalSegments;

                    meshPartBuilder.index((short) (1 + i * horizontalSegments + j));
                    meshPartBuilder.index((short) (1 + i * horizontalSegments + nextJ));
                    meshPartBuilder.index((short) (1 + nextI * horizontalSegments + j));

                    meshPartBuilder.index((short) (1 + i * horizontalSegments + nextJ));
                    meshPartBuilder.index((short) (1 + nextI * horizontalSegments + nextJ));
                    meshPartBuilder.index((short) (1 + nextI * horizontalSegments + j));
                }
            }

            for (int i = 0; i < horizontalSegments; i++) {
                meshPartBuilder.index((short) (vertexCount - 1));
                meshPartBuilder.index((short) (vertexCount - 2 - (i + 1) % horizontalSegments));
                meshPartBuilder.index((short) (vertexCount - 2 - i));
            }

            return modelBuilder.end();
        }
    }
}