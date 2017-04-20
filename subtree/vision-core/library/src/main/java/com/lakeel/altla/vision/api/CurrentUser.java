package com.lakeel.altla.vision.api;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.support.annotation.NonNull;

public final class CurrentUser {

    private static final CurrentUser INSTANCE = new CurrentUser();

    private CurrentUser() {
    }

    @NonNull
    public static CurrentUser getInstance() {
        return INSTANCE;
    }

    @NonNull
    public String getUserId() {
        return getFirebaseUser().getUid();
    }

    @NonNull
    public FirebaseUser getFirebaseUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) throw new IllegalStateException("The user is not authorized.");

        return user;
    }

}
