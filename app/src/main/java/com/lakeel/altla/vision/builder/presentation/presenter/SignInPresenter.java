package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowTangoPermissionViewEvent;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.StringRes;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Defines the presenter for {@link View}.
 */
public final class SignInPresenter extends BasePresenter<SignInPresenter.View> {

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 999;

    @Inject
    VisionService visionService;

    @Inject
    GoogleApiClient googleApiClient;

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

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
                        eventBus.post(ShowTangoPermissionViewEvent.INSTANCE);
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
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(ActionBarVisibleEvent.VISIBLE);
        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_sign_in_view)));
        eventBus.post(HomeAsUpVisibleEvent.INVISIBLE);
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

    public void signIn() {
        signInButtonClicked = true;

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);

        getView().startActivityForResult(intent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_GOOGLE_SIGN_IN) {
            // Ignore.
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            getLog().d("Canceled to sign in to Google.");
            getView().showSnackbar(R.string.snackbar_google_sign_in_reqiured);
            return;
        }

        GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (googleSignInResult == null) {
            getLog().e("GoogleSignInResult is null.");
            getView().showSnackbar(R.string.snackbar_google_sign_in_failed);
            return;
        }

        if (!googleSignInResult.isSuccess()) {
            getLog().e("Failed to sign in to Google.");
            getView().showSnackbar(R.string.snackbar_google_sign_in_failed);
            return;
        }

        GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
        if (googleSignInAccount == null) {
            getLog().e("GoogleSignInAccount is null.");
            getView().showSnackbar(R.string.snackbar_google_sign_in_failed);
            return;
        }

        Disposable disposable = Completable
                .create(e -> {
                    visionService.getAuthApi().signInWithGoogle(googleSignInAccount, aVoid -> {
                        e.onComplete();
                    }, e::onError);
                })
                .doOnSubscribe(_subscription -> getView().showProgressDialog())
                .doOnTerminate(() -> getView().hideProgressDialog())
                .subscribe(() -> {
                    eventBus.post(ShowTangoPermissionViewEvent.INSTANCE);
                }, e -> {
                    getLog().e("Failed to sign in to Firebase.", e);
                });
        compositeDisposable.add(disposable);
    }

    public interface View {

        void startActivityForResult(Intent intent, int requestCode);

        void showSnackbar(@StringRes int resId);

        void showProgressDialog();

        void hideProgressDialog();
    }
}
