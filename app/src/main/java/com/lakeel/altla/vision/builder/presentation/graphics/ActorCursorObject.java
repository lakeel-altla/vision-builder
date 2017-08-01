package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import android.support.annotation.NonNull;

public class ActorCursorObject extends ModelInstance {

    public final Vector3 position = new Vector3();

    public final Quaternion orientation = new Quaternion();

    public final Vector3 direction = new Vector3();

    public final Vector3 up = new Vector3();

    public final Matrix4 view = new Matrix4();

    public ActorCursorObject(@NonNull Model model) {
        super(model);
    }

    public final void update(@NonNull Vector3 cameraPosition, @NonNull Quaternion cameraOrientation) {
        // Calculate the view matrix.
        direction.set(cameraPosition.x, cameraPosition.y, cameraPosition.z - 1);
        up.set(0, 1, 0);
        cameraOrientation.transform(direction);
        cameraOrientation.transform(up);

        view.idt();
        view.setToLookAt(cameraPosition, direction, up);

        // Transform the position (0, 0, -1) in the view space to the world space.
        view.inv();
        position.set(0, 0, -1);
        position.mul(view);

        // Use the camera rotation as an own one.
        orientation.set(cameraOrientation);
        transform.idt();
        transform.rotate(orientation);

        transform.setTranslation(position);
    }
}
