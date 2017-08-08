package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

public final class GeometryObjectManager {

    private final SimpleArrayMap<String, IntArray> indicesMap = new SimpleArrayMap<>();

    private final Array<GeometryObject> objects = new Array<>();

    public int getGeometryObjectCount() {
        return objects.size;
    }

    @NonNull
    public GeometryObject getGeometryObject(int index) {
        return objects.get(index);
    }

    public void addGeometryObject(@NonNull GeometryObject object) {
        final Integer index = objects.size;
        objects.add(object);

        final String actorId = object.actor.getId();
        IntArray indices = indicesMap.get(actorId);
        if (indices == null) {
            indices = new IntArray();
            indicesMap.put(actorId, indices);
        }

        indices.add(index);
    }

    public void removeGeometryObjects(@NonNull String actorId) {
        final IntArray indices = indicesMap.remove(actorId);
        if (indices != null) {
            for (int i = 0; i < indices.size; i++) {
                objects.removeIndex(indices.get(i));
            }
        }
    }
}
