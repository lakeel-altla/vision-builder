package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.builder.presentation.graphics.TangoCamera;
import com.lakeel.altla.vision.model.GeometryComponent;

import android.support.annotation.NonNull;

public final class GeometryCursorObject extends ModelInstance {

    public final Vector3 position = new Vector3();

    public final Quaternion orientation = new Quaternion();

    public final GeometryComponent component;

    private final TangoCamera camera;

    public GeometryCursorObject(@NonNull Model model, @NonNull GeometryComponent component,
                                @NonNull TangoCamera camera) {
        super(model);
        this.component = component;
        this.camera = camera;
    }

    public void update() {
        position.set(0, 0, -1);
        position.mul(camera.invView);

        // Use the camera rotation as an own one.
        orientation.set(camera.orientation);
        transform.idt();
        transform.rotate(orientation);

        transform.setTranslation(position);
    }

    public void saveTransform() {
        component.setPosition(position.x, position.y, position.z);
        component.setOrientation(orientation.x, orientation.y, orientation.z, orientation.w);
        component.setScale(1, 1, 1);
    }
}
