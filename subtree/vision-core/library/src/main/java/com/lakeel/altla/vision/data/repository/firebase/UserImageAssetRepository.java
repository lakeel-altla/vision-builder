package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.ImageAsset;

import android.support.annotation.NonNull;

public final class UserImageAssetRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userImageAssets";

    private static final String FIELD_UPDATED_AT = "updatedAt";

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
                             getLog().e(String.format("Failed to saveActor: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<ImageAsset> find(@NonNull String userId, @NonNull String assetId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(assetId);
        return new TypedQuery<>(reference, ImageAsset.class);
    }

    @NonNull
    public TypedQuery<ImageAsset> findAll(@NonNull String userId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .orderByChild(FIELD_UPDATED_AT);
        return new TypedQuery<>(query, ImageAsset.class);
    }
}
