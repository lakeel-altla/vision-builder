package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.helper.FirebaseObservableData;
import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Profile;

import android.support.annotation.NonNull;

public final class UserProfileRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userProfiles";

    public UserProfileRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull Profile profile) {
        if (profile.getUserId() == null) throw new IllegalArgumentException("profile.getUserId() must be not null.");

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(profile.getUserId())
                     .setValue(profile, (error, reference) -> {
                         if (error != null) {
                             getLog().e("Failed to save.", error.toException());
                         }
                     });
    }

    public void find(@NonNull String userId, OnSuccessListener<Profile> onSuccessListener,
                     OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             Profile profile = snapshot.getValue(Profile.class);
                             onSuccessListener.onSuccess(profile);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    @NonNull
    public ObservableData<Profile> observe(@NonNull String userId) {
        DatabaseReference reference = getDatabase().getReference()
                                                   .child(BASE_PATH)
                                                   .child(userId);

        return new FirebaseObservableData<>(reference, snapshot -> snapshot.getValue(Profile.class));
    }
}
