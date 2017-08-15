package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.presentation.graphics.asset.AssetLoader;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AssetMeshComponent;
import com.lakeel.altla.vision.model.CollisionComponent;
import com.lakeel.altla.vision.model.Component;
import com.lakeel.altla.vision.model.MeshComponent;
import com.lakeel.altla.vision.model.PrimitiveMeshComponent;

import android.support.annotation.NonNull;

public final class ActorNodeFactory {

    private static final Log LOG = LogFactory.getLog(ActorNodeFactory.class);

    private final AssetLoader assetLoader;

    private final ShapeModelFactory shapeModelFactory;

    public ActorNodeFactory(@NonNull AssetLoader assetLoader, @NonNull ShapeModelFactory shapeModelFactory) {
        this.assetLoader = assetLoader;
        this.shapeModelFactory = shapeModelFactory;
    }

    @NonNull
    public ActorNode create(@NonNull Actor actor) {

        final ActorNode node = new ActorNode(actor);

        for (final Component component : actor.getComponents()) {
            if (component instanceof MeshComponent) {
                buildActorMeshInstance(node, (MeshComponent) component);
            } else if (component instanceof CollisionComponent) {
                buildCollisionComponentInstance(node, (CollisionComponent) component);
            }
        }

        return node;
    }

    private void buildActorMeshInstance(@NonNull ActorNode node, @NonNull MeshComponent meshComponent) {

        if (meshComponent instanceof AssetMeshComponent) {

            final AssetMeshComponent component = (AssetMeshComponent) meshComponent;

            final String assetId = component.getRequiredAssetId();
            final String assetType = component.getRequiredAssetType();

            LOG.v("Loading a model: assetId = %s, assetType = %s", assetId, assetType);

            assetLoader.load(Model.class, assetId, assetType, model -> {
                Gdx.app.postRunnable(() -> {
                    final MeshComponentInstance instance = new MeshComponentInstance(model, node);
                    node.addComponentInstance(instance);
                    node.setMainComponentInstance(instance);

                    LOG.v("Added a mesh component instance: actorId = %s, componentClass = %s",
                          node.actor.getId(), component.getClass());
                });
            }, e -> {
                LOG.e("Failed to build the model: assetId = %s, assetType = %s", assetId, assetType);
            });

        } else if (meshComponent instanceof PrimitiveMeshComponent) {

            final PrimitiveMeshComponent component = (PrimitiveMeshComponent) meshComponent;

            final Model model = shapeModelFactory.create(component.getClass());

            final MeshComponentInstance instance = new MeshComponentInstance(model, node);
            node.addComponentInstance(instance);
            node.setMainComponentInstance(instance);

            LOG.v("Added a mesh component instance: actorId = %s, componentClass = %s",
                  node.actor.getId(), component.getClass());

        } else {
            throw new IllegalArgumentException("A type of 'meshComponent' is invalid.");
        }
    }

    private void buildCollisionComponentInstance(@NonNull ActorNode node,
                                                 @NonNull CollisionComponent collisionComponent) {

        final Model model = shapeModelFactory.create(collisionComponent.getClass());
        final CollisionComponentInstance instance = new CollisionComponentInstance(model, node, collisionComponent);
        node.addComponentInstance(instance);

        if (node.getMainComponentInstance() == null) {
            node.setMainComponentInstance(instance);
        }

        LOG.v("Added a collision component instance: actorId = %s, componentClass = %s",
              node.actor.getId(), collisionComponent.getClass());
    }
}
