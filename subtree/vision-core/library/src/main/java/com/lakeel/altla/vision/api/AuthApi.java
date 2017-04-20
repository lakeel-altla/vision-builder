package com.lakeel.altla.vision.api;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import com.lakeel.altla.vision.data.repository.firebase.UserDeviceRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserProfileRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Device;
import com.lakeel.altla.vision.model.Profile;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class AuthApi extends BaseVisionApi {

    private final UserProfileRepository userProfileRepository;

    private final UserDeviceRepository userDeviceRepository;

    public AuthApi(@NonNull VisionService visionService) {
        super(visionService);

        userProfileRepository = new UserProfileRepository(visionService.getFirebaseDatabase());
        userDeviceRepository = new UserDeviceRepository(visionService.getFirebaseDatabase());
    }

    public void signInWithGoogle(@NonNull GoogleSignInAccount googleSignInAccount,
                                 @Nullable OnSuccessListener<Void> onSuccessListener,
                                 @Nullable OnFailureListener onFailureListener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        Task<AuthResult> task = FirebaseAuth.getInstance().signInWithCredential(credential);

        task.addOnSuccessListener(authResult -> {
            FirebaseUser user = authResult.getUser();

            userProfileRepository.find(user.getUid(), profile -> {
                // Create the user profile if it does not exist.
                if (profile == null) {
                    profile = new Profile();
                    profile.setId(user.getUid());
                    profile.setUserId(user.getUid());
                    profile.setDisplayName(user.getDisplayName());
                    profile.setEmail(user.getEmail());
                    if (user.getPhotoUrl() != null) {
                        profile.setPhotoUri(user.getPhotoUrl().toString());
                    }

                    userProfileRepository.save(profile);
                }

                Device device = new Device();
                device.setId(FirebaseInstanceId.getInstance().getId());
                device.setUserId(user.getUid());
                device.setCreatedAtAsLong(FirebaseInstanceId.getInstance().getCreationTime());
                device.setOsName("android");
                device.setOsModel(Build.MODEL);
                device.setOsVersion(Build.VERSION.RELEASE);

                userDeviceRepository.save(device);

                if (onSuccessListener != null) onSuccessListener.onSuccess(null);
            }, onFailureListener);
        });

        task.addOnFailureListener(e -> {
            if (onFailureListener != null) onFailureListener.onFailure(e);
        });
    }
}
