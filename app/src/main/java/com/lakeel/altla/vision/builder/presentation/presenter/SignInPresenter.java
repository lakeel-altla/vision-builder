package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Defines the presenter for {@link View}.
 */
public final class SignInPresenter extends BasePresenter<SignInPresenter.View> {

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 0;

    @Inject
    VisionService visionService;

    @Inject
    GoogleApiClient googleApiClient;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final FirebaseAuth.AuthStateListener authStateListener;

    private boolean signedInDetected;

    private boolean signInButtonClicked;

    @Inject
    public SignInPresenter() {
        // See:
        // http://stackoverflow.com/questions/37674823/firebase-android-onauthstatechanged-fire-twice-after-signinwithemailandpasswor
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                if (!signedInDetected) {
                    getLog().i("Signed in to firebase: %s", user.getUid());
                    if (!signInButtonClicked) {
                        getView().onCloseSignInView();
                    }
                    signedInDetected = true;
                } else {
                    getLog().d("onAuthStateChanged() is fired twice.");
                }
            } else {
                getLog().d("Signed out.");
            }
        };
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        compositeDisposable.clear();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    public void onSignIn() {
        signInButtonClicked = true;

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);

        getView().onStartActivityForResult(intent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_GOOGLE_SIGN_IN) {
            // Ignore.
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            getLog().d("Canceled to sign in to Google.");
            getView().onSnackbar(R.string.snackbar_google_sign_in_reqiured);
            return;
        }

        GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (!googleSignInResult.isSuccess()) {
            getLog().e("Failed to sign in to Google.");
            getView().onSnackbar(R.string.snackbar_google_sign_in_failed);
            return;
        }

        GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
        if (googleSignInAccount == null) {
            getLog().e("GoogleSignInAccount is null");
            getView().onSnackbar(R.string.snackbar_google_sign_in_failed);
            return;
        }

        Disposable disposable = Completable
                .create(e -> {
                    visionService.getAuthApi().signInWithGoogle(googleSignInAccount, aVoid -> {
                        e.onComplete();
                    }, e::onError);
                })
                .doOnSubscribe(_subscription -> getView().onShowProgressDialog())
                .doOnTerminate(() -> getView().onHideProgressDialog())
                .subscribe(() -> {
                               getView().onCloseSignInView();
                           },
                           e -> {
                               getLog().e("Failed to sign in to Firebase.", e);
                           });
        compositeDisposable.add(disposable);
    }

    public interface View {

        void onCloseSignInView();

        void onStartActivityForResult(@NonNull Intent intent, int requestCode);

        void onSnackbar(@StringRes int resId);

        void onShowProgressDialog();

        void onHideProgressDialog();
    }
}
