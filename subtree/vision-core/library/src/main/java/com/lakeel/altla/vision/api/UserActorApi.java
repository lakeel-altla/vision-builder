package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserActorRepository;
import com.lakeel.altla.vision.helper.ComponentTypeResolver;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.BoxComponent;
import com.lakeel.altla.vision.model.Component;
import com.lakeel.altla.vision.model.MeshComponent;
import com.lakeel.altla.vision.model.SphereComponent;
import com.lakeel.altla.vision.model.TransformComponent;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

public final class UserActorApi extends BaseVisionApi {

    private final UserActorRepository userActorRepository;

    public UserActorApi(@NonNull VisionService visionService) {
        super(visionService);

        final ComponentTypeResolver componentTypeResolver = new ComponentTypeResolver() {

            private final SimpleArrayMap<String, Class<? extends Component>> classMap = new SimpleArrayMap<>();

            {
                classMap.put(TransformComponent.TYPE, TransformComponent.class);
                classMap.put(MeshComponent.TYPE, MeshComponent.class);
                classMap.put(BoxComponent.TYPE, BoxComponent.class);
                classMap.put(SphereComponent.TYPE, SphereComponent.class);
            }

            @Override
            public Class<? extends Component> resolve(@NonNull String type) {
                return classMap.get(type);
            }
        };

        userActorRepository = new UserActorRepository(visionService.getFirebaseDatabase(), componentTypeResolver);
    }

    @NonNull
    public TypedQuery<Actor> findActor(@NonNull String areaId, @NonNull String actorId) {
        return userActorRepository.find(CurrentUser.getInstance().getUserId(), areaId, actorId);
    }

    @NonNull
    public TypedQuery<Actor> findActorsByAreaId(@NonNull String areaId) {
        return userActorRepository.findByAreaId(CurrentUser.getInstance().getUserId(), areaId);
    }

    public void saveActor(@NonNull String areaId, @NonNull Actor actor) {
        getVisionService().throwsIfUserIdInvalid(actor);

        userActorRepository.save(areaId, actor);
    }

    public void deleteActor(@NonNull String areaId, @NonNull Actor actor) {
        getVisionService().throwsIfUserIdInvalid(actor);

        userActorRepository.delete(areaId, actor);
    }
}
