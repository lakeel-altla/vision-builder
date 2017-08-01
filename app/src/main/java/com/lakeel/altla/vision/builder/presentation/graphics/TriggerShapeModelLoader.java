package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;
import com.lakeel.altla.vision.model.TriggerShape;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;

public final class TriggerShapeModelLoader implements Disposable {

    private final SimpleArrayMap<TriggerShape, Loader> loaderMap = new SimpleArrayMap<>();

    private final SimpleArrayMap<TriggerShape, Model> modelMap = new SimpleArrayMap<>();

    public TriggerShapeModelLoader() {
        loaderMap.put(TriggerShape.BOX, new BoxModelLoader());
        loaderMap.put(TriggerShape.SPHERE, new SphereModelLoader());
    }

    @Override
    public void dispose() {
        for (int i = 0; i < modelMap.size(); i++) {
            modelMap.valueAt(i).dispose();
        }
        modelMap.clear();
    }

    @NonNull
    public Model load(@NonNull TriggerShape triggerShape) {
        Model model = modelMap.get(triggerShape);
        if (model == null) {
            final Loader loader = loaderMap.get(triggerShape);
            if (loader == null) throw new IllegalArgumentException("An unexpected trigger shape: " + triggerShape);

            model = loader.load();
            modelMap.put(triggerShape, model);
        }
        return model;
    }

    private interface Loader {

        Model load();
    }

    private final class BoxModelLoader implements Loader {

        private static final float SCALE = 0.25f;

        private final ModelBuilder modelBuilder = new ModelBuilder();

        private final Material faceMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW),
                                                           new BlendingAttribute(0.5f));

        private final Material lineMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW));

        @Override
        @NonNull
        public Model load() {
            final float s = SCALE;

            modelBuilder.begin();

            modelBuilder.part("faces", GL20.GL_TRIANGLES, Position, faceMaterial)
                        .box(s, s, s);
            modelBuilder.part("lines", GL20.GL_LINES, Position, lineMaterial)
                        .box(s, s, s);

            return modelBuilder.end();
        }
    }

    private final class SphereModelLoader implements Loader {

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

            modelBuilder.part("faces", GL20.GL_TRIANGLES, Position, faceMaterial)
                        .sphere(s, s, s, 8, 8);
            modelBuilder.part("lines", GL20.GL_LINES, Position, lineMaterial)
                        .sphere(s, s, s, 8, 8);

            return modelBuilder.end();
        }
    }
}
