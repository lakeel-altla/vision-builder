package com.lakeel.altla.vision.builder.presentation.graphics.loader;

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
import com.lakeel.altla.vision.model.BoxComponent;
import com.lakeel.altla.vision.model.ShapeComponent;
import com.lakeel.altla.vision.model.SphereComponent;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;

public final class ShapeModelLoader implements Disposable {

    private final SimpleArrayMap<Class<? extends ShapeComponent>, Loader> loaderMap = new SimpleArrayMap<>();

    private final SimpleArrayMap<Class<? extends ShapeComponent>, Model> modelMap = new SimpleArrayMap<>();

    public ShapeModelLoader() {
        loaderMap.put(BoxComponent.class, new BoxModelLoader());
        loaderMap.put(SphereComponent.class, new SphereModelLoader());
    }

    @Override
    public void dispose() {
        for (int i = 0; i < modelMap.size(); i++) {
            modelMap.valueAt(i).dispose();
        }
        modelMap.clear();
    }

    @NonNull
    public Model load(@NonNull Class<? extends ShapeComponent> clazz) {
        Model model = modelMap.get(clazz);
        if (model == null) {
            final Loader loader = loaderMap.get(clazz);
            if (loader == null) throw new IllegalArgumentException("An unexpected shape component: " + clazz);

            model = loader.load();
            modelMap.put(clazz, model);
        }
        return model;
    }

    private interface Loader {

        Model load();
    }

    private static final class BoxModelLoader implements Loader {

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

        BoxModelLoader() {
            for (int i = 0; i < vertexInfos.length; i++) {
                vertexInfos[i] = new MeshPartBuilder.VertexInfo();
            }
        }

        @Override
        @NonNull
        public Model load() {
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

    private static final class SphereModelLoader implements Loader {

        private static final float SCALE = 0.5f;

        private final ModelBuilder modelBuilder = new ModelBuilder();

        private final Material faceMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW),
                                                           new BlendingAttribute(0.5f));

        private final Material lineMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW));

        @Override
        @NonNull
        public Model load() {
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
