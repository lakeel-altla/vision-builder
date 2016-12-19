package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.rx.firebase.database.RxFirebaseQuery;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.domain.model.UserAreaDescription;
import com.lakeel.altla.vision.domain.repository.UserAreaDescriptionRepository;

import rx.Observable;
import rx.Single;

public final class UserAreaDescriptionRepositoryImpl implements UserAreaDescriptionRepository {

    private static final Log LOG = LogFactory.getLog(UserAreaDescriptionRepositoryImpl.class);

    private static final String PATH_USER_AREA_DESCRIPTIONS = "userAreaDescriptions";

    private final DatabaseReference rootReference;

    public UserAreaDescriptionRepositoryImpl(DatabaseReference rootReference) {
        if (rootReference == null) throw new ArgumentNullException("rootReference");

        this.rootReference = rootReference;
    }

    @Override
    public Single<UserAreaDescription> save(UserAreaDescription userAreaDescription) {
        if (userAreaDescription == null) throw new ArgumentNullException("userAreaDescription");

        UserAreaDescriptionValue value = new UserAreaDescriptionValue();
        value.name = userAreaDescription.name;
        value.creationTime = userAreaDescription.creationTime;

        rootReference.child(PATH_USER_AREA_DESCRIPTIONS)
                     .child(resolveCurrentUserId())
                     .child(userAreaDescription.areaDescriptionId)
                     .setValue(value, (error, reference) -> {
                         if (error != null) {
                             LOG.e(String.format("Failed to save: reference = %s", reference), error.toException());
                         }
                     });

        return Single.just(userAreaDescription);
    }

    @Override
    public Observable<UserAreaDescription> find(String areaDescriptionId) {
        if (areaDescriptionId == null) throw new ArgumentNullException("areaDescriptionId");

        DatabaseReference reference = rootReference.child(PATH_USER_AREA_DESCRIPTIONS)
                                                   .child(resolveCurrentUserId())
                                                   .child(areaDescriptionId);

        return RxFirebaseQuery.asObservableForSingleValueEvent(reference)
                              .filter(DataSnapshot::exists)
                              .map(this::map);
    }

    @Override
    public Observable<UserAreaDescription> findAll() {
        Query query = rootReference.child(PATH_USER_AREA_DESCRIPTIONS)
                                   .child(resolveCurrentUserId())
                                   .orderByValue();

        return RxFirebaseQuery.asObservableForSingleValueEvent(query)
                              .flatMap(snapshot -> Observable.from(snapshot.getChildren()))
                              .map(this::map);
    }

    @Override
    public Single<String> delete(String areaDescriptionId) {
        if (areaDescriptionId == null) throw new ArgumentNullException("areaDescriptionId");

        rootReference.child(PATH_USER_AREA_DESCRIPTIONS)
                     .child(resolveCurrentUserId())
                     .child(areaDescriptionId)
                     .removeValue((error, reference) -> {
                         if (error != null) {
                             LOG.e(String.format("Failed to remove: reference = %s", reference), error.toException());
                         }
                     });

        return Single.just(areaDescriptionId);
    }

    private UserAreaDescription map(DataSnapshot snapshot) {
        String id = snapshot.getKey();
        UserAreaDescriptionValue value = snapshot.getValue(UserAreaDescriptionValue.class);
        return new UserAreaDescription(id, value.name, value.creationTime);
    }

    private String resolveCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("The current user could not be resolved.");
        }
        return user.getUid();
    }

    public static final class UserAreaDescriptionValue {

        public String name;

        public long creationTime;
    }
}
