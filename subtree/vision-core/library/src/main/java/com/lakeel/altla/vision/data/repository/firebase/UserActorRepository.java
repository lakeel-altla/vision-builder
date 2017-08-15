package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.helper.ComponentTypeResolver;
import com.lakeel.altla.vision.helper.DataSnapshotConverter;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.BaseEntity;
import com.lakeel.altla.vision.model.Component;
import com.lakeel.altla.vision.model.TransformComponent;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class UserActorRepository extends BaseDatabaseRepository {

    private static final Log LOG = LogFactory.getLog(UserActorRepository.class);

    private static final String BASE_PATH = "userActors";

    private final ActorConverter converter;

    public UserActorRepository(@NonNull FirebaseDatabase database,
                               @NonNull ComponentTypeResolver componentTypeResolver) {
        super(database);
        converter = new ActorConverter(new ComponentConverter(componentTypeResolver));
    }

    public void save(@NonNull String areaId, @NonNull Actor actor) {
        actor.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(actor.getRequiredUserId())
                     .child(areaId)
                     .child(actor.getId())
                     .setValue(actor, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<Actor> find(@NonNull String userId, @NonNull String areaId, @NonNull String actorId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(areaId)
                                                         .child(actorId);
        return new TypedQuery<>(reference, converter);
    }

    @NonNull
    public TypedQuery<Actor> findByAreaId(@NonNull String userId, @NonNull String areaId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .child(areaId);
        return new TypedQuery<>(query, converter);
    }

    public void delete(@NonNull String areaId, @NonNull Actor actor) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(actor.getRequiredUserId())
                     .child(areaId)
                     .child(actor.getId())
                     .removeValue((error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to remove: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    private static final class ActorConverter implements DataSnapshotConverter<Actor> {

        private final ComponentConverter componentConverter;

        private ActorConverter(@NonNull ComponentConverter componentConverter) {
            this.componentConverter = componentConverter;
        }

        @Override
        public Actor convert(@NonNull DataSnapshot snapshot) {
            final Actor actor = new Actor();

            actor.setId(snapshot.child(BaseEntity.FIELD_ID).getValue(String.class));

            actor.setUserId(snapshot.child(BaseEntity.FIELD_USER_ID).getValue(String.class));

            actor.setGroupId(snapshot.child(BaseEntity.FIELD_GROUP_ID).getValue(String.class));

            final Long createdAt = snapshot.child(BaseEntity.FIELD_CREATED_AT).getValue(Long.class);
            actor.setCreatedAtAsLong(createdAt == null ? -1 : createdAt);

            final Long updatedAt = snapshot.child(BaseEntity.FIELD_UPDATED_AT).getValue(Long.class);
            actor.setUpdatedAtAsLong(updatedAt == null ? -1 : updatedAt);

            actor.setName(snapshot.child(Actor.FIELD_NAME).getValue(String.class));

            final DataSnapshot transformComponentSnapshot = snapshot.child(Actor.FIELD_TRANSFORM_COMPONENT);
            actor.setTransformComponent((TransformComponent) componentConverter.convert(transformComponentSnapshot));

            final DataSnapshot componentsSnapshot = snapshot.child(Actor.FIELD_COMPONENTS);
            final List<Component> components = new ArrayList<>((int) componentsSnapshot.getChildrenCount());
            actor.setComponents(components);
            for (final DataSnapshot componentSnapshot : componentsSnapshot.getChildren()) {
                components.add(componentConverter.convert(componentSnapshot));
            }

            return actor;
        }
    }

    private static final class ComponentConverter implements DataSnapshotConverter<Component> {

        private final ComponentTypeResolver componentTypeResolver;

        private ComponentConverter(@NonNull ComponentTypeResolver componentTypeResolver) {
            this.componentTypeResolver = componentTypeResolver;
        }

        @Override
        public Component convert(@NonNull DataSnapshot snapshot) {
            final String type = (String) snapshot.child(Component.FIELD_TYPE).getValue();
            if (type == null) {
                throw new IllegalStateException("The field 'type' could not be found.");
            }

            final Class<? extends Component> clazz = componentTypeResolver.resolve(type);
            if (clazz == null) {
                throw new IllegalStateException("The component class could not be found: type = " + type);
            }

            return snapshot.getValue(clazz);
        }
    }
}
