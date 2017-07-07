package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.tango.TangoIntents;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class SignInActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private static final Log LOG = LogFactory.getLog(SignInActivity.class);

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 111;

    private static final int REQUEST_CODE_ADF_LOAD_SAVE_REQUEST_PERMISSIONS = 222;

    @Inject
    VisionService visionService;

    @BindView(R.id.sign_in_button)
    SignInButton signInButton;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ActivityComponent activityComponent;

    private GoogleApiClient googleApiClient;

    private ProgressDialog progressDialog;

    private boolean signedInDetected;

    @NonNull
    public static Intent createIntent(@NonNull Activity activity) {
        return new Intent(activity, SignInActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // NOTE:
        //
        // Any injection must be done before super.onCreate()
        // because fragments are already attached to an activity when they are resumed or instant-run.
        activityComponent = MyApplication.getApplicationComponent(this)
                                         .activityComponent(new ActivityModule(this));
        activityComponent.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_sign_in_view);
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            LOG.w("ActionBar is null.");
        }

        final GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionResult -> {
                    LOG.e("Google API connection error occured: %s", connectionResult);
                    Toast.makeText(this, R.string.toast_google_api_client_connection_failed, Toast.LENGTH_LONG)
                         .show();
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        signInButton.setSize(SignInButton.SIZE_STANDARD);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseAuth.getInstance().removeAuthStateListener(this);
        compositeDisposable.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_GOOGLE_SIGN_IN:
                handleGoogleSignInResult(resultCode, data);
                break;
            case REQUEST_CODE_ADF_LOAD_SAVE_REQUEST_PERMISSIONS:
                handleAdfLoadSaveRequestPermissions(resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            if (!signedInDetected) {
                LOG.i("Signed in to firebase: %s", user.getUid());
                signInButton.setVisibility(View.GONE);
                Intent intent = TangoIntents.createAdfLoadSaveRequestPermissionIntent();
                startActivityForResult(intent, REQUEST_CODE_ADF_LOAD_SAVE_REQUEST_PERMISSIONS);
                signedInDetected = true;
            } else {
                LOG.d("onAuthStateChanged() is fired twice.");
            }
        } else {
            LOG.d("Signed out.");
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.sign_in_button)
    void onClickButtonSignIn() {
        signIn();
    }

    private void signIn() {
        signInButton.setEnabled(false);

        final Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void handleGoogleSignInResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            LOG.d("Canceled to sign in to Google.");
            signInButton.setEnabled(true);
            return;
        }

        GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (googleSignInResult == null) {
            LOG.e("GoogleSignInResult is null.");
            Toast.makeText(this, R.string.toast_google_sign_in_failed, Toast.LENGTH_SHORT).show();
            signInButton.setEnabled(true);
            return;
        }

        if (!googleSignInResult.isSuccess()) {
            LOG.e("Failed to sign in to Google.");
            Toast.makeText(this, R.string.toast_google_sign_in_failed, Toast.LENGTH_SHORT).show();
            signInButton.setEnabled(true);
            return;
        }

        GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
        if (googleSignInAccount == null) {
            LOG.e("GoogleSignInAccount is null.");
            Toast.makeText(this, R.string.toast_google_sign_in_failed, Toast.LENGTH_SHORT).show();
            signInButton.setEnabled(true);
            return;
        }

        showProgressDialog();

        Disposable disposable = Completable
                .create(e -> {
                    visionService.getAuthApi().signInWithGoogle(googleSignInAccount, aVoid -> {
                        e.onComplete();
                    }, e::onError);
                })
                .subscribe(() -> {
                    hideProgressDialog();
                }, e -> {
                    signInButton.setEnabled(true);
                    hideProgressDialog();
                    LOG.e("Failed to sign in to Firebase.", e);
                });
        compositeDisposable.add(disposable);
    }

    private void handleAdfLoadSaveRequestPermissions(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // TODO: Request the camera permission too for Android 6.0 or later.
            Intent intent = ArActivity.createIntent(this);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.toast_adf_save_load_permissions_required, Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progress_dialog_signin_in));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
