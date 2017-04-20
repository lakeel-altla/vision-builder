package com.lakeel.altla.vision.api;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import com.lakeel.altla.tango.TangoWrapper;

import android.content.Context;
import android.support.annotation.NonNull;

public final class VisionService {

    private final Context context;

    private final FirebaseDatabase firebaseDatabase;

    private final FirebaseStorage firebaseStorage;

    private final TangoWrapper tangoWrapper;

    private final GooglePlaceApi googlePlaceApi;

    private final TangoAreaDescriptionApi tangoAreaDescriptionApi;

    private final FirebaseConnectionApi firebaseConnectionApi;

    private final AuthApi authApi;

    private final UserDeviceConnectionApi userDeviceConnectionApi;

    private final UserProfileApi userProfileApi;

    private final UserAreaApi userAreaApi;

    private final UserAreaDescriptionApi userAreaDescriptionApi;

    private final UserAssetApi userAssetApi;

    private final UserActorApi userActorApi;

    // TODO: This api is used our builder only.
    private final UserAreaSettingsApi userAreaSettingsApi;

    private final PublicAreaApi publicAreaApi;

    private final PublicAreaDescriptionApi publicAreaDescriptionApi;

    private final PublicActorApi publicActorApi;

    public VisionService(@NonNull Context context,
                         @NonNull FirebaseDatabase firebaseDatabase,
                         @NonNull FirebaseStorage firebaseStorage) {
        this.context = context;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseStorage = firebaseStorage;

        tangoWrapper = new TangoWrapper(context);

        googlePlaceApi = new GooglePlaceApi(this);

        tangoAreaDescriptionApi = new TangoAreaDescriptionApi(this);

        firebaseConnectionApi = new FirebaseConnectionApi(this);
        authApi = new AuthApi(this);
        userDeviceConnectionApi = new UserDeviceConnectionApi(this);
        userProfileApi = new UserProfileApi(this);
        userAreaApi = new UserAreaApi(this);
        userAreaDescriptionApi = new UserAreaDescriptionApi(this);
        userAssetApi = new UserAssetApi(this);
        userActorApi = new UserActorApi(this);
        userAreaSettingsApi = new UserAreaSettingsApi(this);

        publicAreaApi = new PublicAreaApi(this);
        publicAreaDescriptionApi = new PublicAreaDescriptionApi(this);
        publicActorApi = new PublicActorApi(this);
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @NonNull
    FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    @NonNull
    FirebaseStorage getFirebaseStorage() {
        return firebaseStorage;
    }

    @NonNull
    public TangoWrapper getTangoWrapper() {
        return tangoWrapper;
    }

    @NonNull
    public GooglePlaceApi getGooglePlaceApi() {
        return googlePlaceApi;
    }

    @NonNull
    public TangoAreaDescriptionApi getTangoAreaDescriptionApi() {
        return tangoAreaDescriptionApi;
    }

    @NonNull
    public FirebaseConnectionApi getFirebaseConnectionApi() {
        return firebaseConnectionApi;
    }

    @NonNull
    public AuthApi getAuthApi() {
        return authApi;
    }

    @NonNull
    public UserDeviceConnectionApi getUserDeviceConnectionApi() {
        return userDeviceConnectionApi;
    }

    @NonNull
    public UserProfileApi getUserProfileApi() {
        return userProfileApi;
    }

    @NonNull
    public UserAreaApi getUserAreaApi() {
        return userAreaApi;
    }

    @NonNull
    public UserAreaDescriptionApi getUserAreaDescriptionApi() {
        return userAreaDescriptionApi;
    }

    @NonNull
    public UserAssetApi getUserAssetApi() {
        return userAssetApi;
    }

    @NonNull
    public UserActorApi getUserActorApi() {
        return userActorApi;
    }

    @NonNull
    public UserAreaSettingsApi getUserAreaSettingsApi() {
        return userAreaSettingsApi;
    }

    @NonNull
    public PublicAreaApi getPublicAreaApi() {
        return publicAreaApi;
    }

    @NonNull
    public PublicAreaDescriptionApi getPublicAreaDescriptionApi() {
        return publicAreaDescriptionApi;
    }

    @NonNull
    public PublicActorApi getPublicActorApi() {
        return publicActorApi;
    }
}
