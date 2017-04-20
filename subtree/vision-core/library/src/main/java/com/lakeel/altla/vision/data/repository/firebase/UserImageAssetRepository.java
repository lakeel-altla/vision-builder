package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.helper.FirebaseObservableData;
import com.lakeel.altla.vision.helper.FirebaseObservableList;
import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.ObservableList;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.ImageAsset;

import android.support.annotation.NonNull;

public final class UserImageAssetRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userImageAssets";

    private static final String FIELD_NAME = "name";

    public UserImageAssetRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull ImageAsset asset) {
        if (asset.getUserId() == null) {
            throw new IllegalArgumentException("asset.getUserId() must be not null.");
        }

        asset.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(asset.getUserId())
                     .child(asset.getId())
                     .setValue(asset, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    public void find(@NonNull String userId, @NonNull String assetId,
                     OnSuccessListener<ImageAsset> onSuccessListener, OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(assetId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             ImageAsset userImageAsset = snapshot.getValue(ImageAsset.class);
                             if (onSuccessListener != null) onSuccessListener.onSuccess(userImageAsset);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             if (onFailureListener != null) onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    @NonNull
    public ObservableData<ImageAsset> observe(@NonNull String userId, @NonNull String assetId) {
        DatabaseReference reference = getDatabase().getReference()
                                                   .child(BASE_PATH)
                                                   .child(userId)
                                                   .child(assetId);

        return new FirebaseObservableData<>(reference, snapshot -> snapshot.getValue(ImageAsset.class));
    }

    @NonNull
    public ObservableList<ImageAsset> observeAll(@NonNull String userId) {
        Query query = getDatabase().getReference()
                                   .child(BASE_PATH)
                                   .child(userId)
                                   .orderByChild(FIELD_NAME);

        return new FirebaseObservableList<>(query, snapshot -> snapshot.getValue(ImageAsset.class));
    }
}
