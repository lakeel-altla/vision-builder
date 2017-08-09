package com.lakeel.altla.vision.builder.presentation.model;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Asset;
import com.lakeel.altla.vision.model.MeshComponent;
import com.lakeel.altla.vision.model.ShapeComponent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public final class ArModel {

    private static final Log LOG = LogFactory.getLog(ArModel.class);

    @NonNull
    private final VisionService visionService;

    @Nullable
    private AreaSettings areaSettings;

    @Nullable
    private Actor selectedActor;

    public ArModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @Nullable
    public synchronized AreaSettings getAreaSettings() {
        return areaSettings;
    }

    public synchronized void setAreaSettings(@Nullable AreaSettings areaSettings) {
        this.areaSettings = areaSettings;
    }

    @NonNull
    public synchronized AreaSettings getRequiredAreaSettings() {
        if (areaSettings == null) throw new IllegalStateException("'areaSettings' is null.");
        return areaSettings;
    }

    public synchronized boolean canEdit() {
        return areaSettings != null;
    }

    @Nullable
    public Actor getSelectedActor() {
        return selectedActor;
    }

    public void setSelectedActor(@Nullable Actor selectedActor) {
        this.selectedActor = selectedActor;

        if (selectedActor == null) {
            LOG.d("No actor is selected.");
        } else {
            LOG.d("An actor is selected.");
        }
    }

    @NonNull
    public Actor getRequiredSelectedActor() {
        if (selectedActor == null) throw new IllegalStateException("'selectedActor' is null.");
        return selectedActor;
    }

    @NonNull
    public synchronized TypedQuery<Actor> loadUserActors() {
        return visionService.getUserActorApi()
                            .findActorsByAreaId(getRequiredAreaSettings().getRequiredAreaId());
    }

    public void saveSelectedActor() {
        visionService.getUserActorApi()
                     .saveActor(getRequiredAreaSettings().getRequiredAreaId(), getRequiredSelectedActor());
    }

    public void deleteSelectedActor() {
        visionService.getUserActorApi()
                     .deleteActor(getRequiredAreaSettings().getRequiredAreaId(), getRequiredSelectedActor());
    }

    public void saveActor(@NonNull Actor actor) {
        visionService.getUserActorApi()
                     .saveActor(getRequiredAreaSettings().getRequiredAreaId(), actor);
    }

    public void saveMeshActor(@NonNull Asset asset, @NonNull Vector3 position, @NonNull Quaternion orientation,
                              @NonNull Vector3 scale) {
        final Actor actor = new Actor();

        actor.setUserId(CurrentUser.getInstance().getUserId());
        actor.setName(asset.getName());

        final MeshComponent meshComponent = new MeshComponent();
        actor.addComponent(meshComponent);

        meshComponent.setAssetId(asset.getId());
        meshComponent.setAssetType(asset.getType());
        meshComponent.setPosition(position.x, position.y, position.z);
        meshComponent.setOrientation(orientation.x, orientation.y, orientation.z, orientation.w);
        meshComponent.setScale(scale.x, scale.y, scale.z);
        meshComponent.setVisible(true);
        meshComponent.setVisibleAtRuntime(true);

        saveActor(actor);
    }

    public void saveTriggerActor(@NonNull Class<? extends ShapeComponent> clazz,
                                 @NonNull Vector3 position, @NonNull Quaternion orientation, @NonNull Vector3 scale) {
        final Actor actor = new Actor();

        actor.setUserId(CurrentUser.getInstance().getUserId());
        // TODO
        actor.setName(clazz.getSimpleName());

        try {
            final ShapeComponent shapeComponent = clazz.newInstance();
            actor.addComponent(shapeComponent);

            shapeComponent.setPosition(position.x, position.y, position.z);
            shapeComponent.setOrientation(orientation.x, orientation.y, orientation.z, orientation.w);
            shapeComponent.setScale(scale.x, scale.y, scale.z);
            shapeComponent.setVisible(true);
            shapeComponent.setVisibleAtRuntime(false);

            saveActor(actor);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to instantiate a ShapeComponent.", e);
        }
    }

    public void loadAssetFile(@NonNull String assetId,
                              @Nullable OnSuccessListener<File> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener,
                              @Nullable OnProgressListener onProgressListener) {
        visionService.getUserAssetApi()
                     .loadAssetFile(assetId, onSuccessListener, onFailureListener, onProgressListener);
    }
}
