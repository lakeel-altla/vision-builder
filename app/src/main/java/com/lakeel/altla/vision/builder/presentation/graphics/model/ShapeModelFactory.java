package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.model.BoxCollisionComponent;
import com.lakeel.altla.vision.model.BoxMeshComponent;
import com.lakeel.altla.vision.model.Component;
import com.lakeel.altla.vision.model.SphereCollisionComponent;
import com.lakeel.altla.vision.model.SphereMeshComponent;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;

public final class ShapeModelFactory implements Disposable {

    private static final Log LOG = LogFactory.getLog(ShapeModelFactory.class);

    private static final int SHAPE_BOX = 0;

    private static final int SHAPE_SPHERE = 1;

    private final SparseArrayCompat<Builder> builderMap = new SparseArrayCompat<>();

    private final SparseArrayCompat<Model> modelMap = new SparseArrayCompat<>();

    public ShapeModelFactory() {
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

        private static final float SIZE = 0.25f;

        private static final float HALF_FIZE = SIZE * 0.5f;

        private static final Vector3[] NORMALS = {
                new Vector3(0, 1, 0),
                new Vector3(0, -1, 0),
                new Vector3(0, 0, -1),
                new Vector3(0, 0, 1),
                new Vector3(-1, 0, 0),
                new Vector3(1, 0, 0)
        };

        private final ModelBuilder modelBuilder = new ModelBuilder();

        private final Material faceMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW),
                                                           new BlendingAttribute(0.5f));

        private final Material lineMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW));

        private final Vector3 side1 = new Vector3();

        private final Vector3 side2 = new Vector3();

        private final MeshPartBuilder.VertexInfo[] vertexInfos = new MeshPartBuilder.VertexInfo[4];

        private MeshPartBuilder.VertexInfo vertexInfo = new MeshPartBuilder.VertexInfo();

        private final Vector3 position = new Vector3();

        BoxModelBuilder() {
            for (int i = 0; i < vertexInfos.length; i++) {
                vertexInfos[i] = new MeshPartBuilder.VertexInfo();
            }
        }

        @Override
        @NonNull
        public Model build() {
//            final float s = SIZE;

            modelBuilder.begin();

            for (int i = 0; i < NORMALS.length; i++) {
                final MeshPartBuilder meshPartBuilder = modelBuilder.part("face_" + i,
                                                                          GL20.GL_TRIANGLES,
                                                                          Position | Normal,
                                                                          faceMaterial);

                final Vector3 normal = NORMALS[i];
                side1.set(normal.y, normal.z, normal.x);
                side2.set(side1).crs(normal);
//                side2.set(normal).crs(side1);

                vertexInfo.set(position.set(normal).sub(side1).sub(side2).scl(HALF_FIZE), normal, null, null);
                final short index0 = meshPartBuilder.vertex(vertexInfo);

                vertexInfo.set(position.set(normal).sub(side1).add(side2).scl(HALF_FIZE), normal, null, null);
                final short index1 = meshPartBuilder.vertex(vertexInfo);

                vertexInfo.set(position.set(normal).add(side1).add(side2).scl(HALF_FIZE), normal, null, null);
                final short index2 = meshPartBuilder.vertex(vertexInfo);

                vertexInfo.set(position.set(normal).add(side1).sub(side2).scl(HALF_FIZE), normal, null, null);
                final short index3 = meshPartBuilder.vertex(vertexInfo);

                meshPartBuilder.index(index0);
                meshPartBuilder.index(index1);
                meshPartBuilder.index(index2);
                meshPartBuilder.index(index0);
                meshPartBuilder.index(index2);
                meshPartBuilder.index(index3);
            }

//            modelBuilder.part("faces", GL20.GL_TRIANGLES, Position | Normal, faceMaterial)
//                        .box(s, s, s);
//            modelBuilder.part("lines", GL20.GL_LINES, Position, lineMaterial)
//                        .box(s, s, s);

            return modelBuilder.end();
        }
    }

    private static final class SphereModelBuilder implements Builder {

        private static final float SCALE = 0.5f;

        private final ModelBuilder modelBuilder = new ModelBuilder();

        private final Material faceMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW),
                                                           new BlendingAttribute(0.5f));

        private final Material lineMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW));

        @Override
        @NonNull
        public Model build() {
            final float s = SCALE;

            modelBuilder.begin();

            modelBuilder.part("faces", GL20.GL_TRIANGLES, Position | Normal, faceMaterial)
                        .sphere(s, s, s, 8, 8);
            modelBuilder.part("lines", GL20.GL_LINES, Position, lineMaterial)
                        .sphere(s, s, s, 8, 8);

            return modelBuilder.end();
        }
    }
}
