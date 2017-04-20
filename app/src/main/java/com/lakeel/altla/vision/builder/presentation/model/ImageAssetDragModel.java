package com.lakeel.altla.vision.builder.presentation.model;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ImageAssetDragModel {

    private static final String EXTRA_USER_ID = "userId";

    private static final String EXTRA_ASSET_ID = "assetId";

    private static final String EXTRA_URI = "uri";

    private static final String EXTRA_NAME = "name";

    public String userId;

    public String assetId;

    public String name;

    public Uri uri;

    public Intent createIntent() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_ASSET_ID, assetId);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_URI, uri);
        return intent;
    }

    @Nullable
    public static ImageAssetDragModel parseIntent(@NonNull Intent intent) {
        ImageAssetDragModel model = new ImageAssetDragModel();
        model.userId = intent.getStringExtra(EXTRA_USER_ID);
        model.assetId = intent.getStringExtra(EXTRA_ASSET_ID);
        model.name = intent.getStringExtra(EXTRA_NAME);
        model.uri = intent.getParcelableExtra(EXTRA_URI);
        return model;
    }
}
