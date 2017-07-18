package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.model.Asset;
import com.lakeel.altla.vision.model.AssetType;

import android.support.annotation.NonNull;

public final class CursorObject extends ModelInstance {

    public final Asset asset;

    public final AssetType assetType;

    public final Vector3 position = new Vector3();

    public final Quaternion rotation = new Quaternion();

    public final Vector3 direction = new Vector3();

    public final Vector3 up = new Vector3();

    public final Matrix4 view = new Matrix4();

    public CursorObject(@NonNull Model model, @NonNull Asset asset, @NonNull AssetType assetType) {
        super(model);
        this.asset = asset;
        this.assetType = assetType;
    }

    public void update(@NonNull Vector3 cameraPosition, @NonNull Quaternion cameraRotation) {
        // Calculate the view matrix.
        direction.set(cameraPosition.x, cameraPosition.y, cameraPosition.z - 1);
        up.set(0, 1, 0);
        cameraRotation.transform(direction);
        cameraRotation.transform(up);

        view.idt();
        view.setToLookAt(cameraPosition, direction, up);

        // Transform the position (0, 0, -1) in the view space to the world space.
        view.inv();
        position.set(0, 0, -1);
        position.mul(view);

        // Use the camera rotation as an own one.
        rotation.set(cameraRotation);
        transform.idt();
        transform.rotate(rotation);

        transform.setTranslation(position);
    }
}
