package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.SignInPresenter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class SignInFragment extends AbstractFragment<SignInPresenter.View, SignInPresenter>
        implements SignInPresenter.View {

    @Inject
    SignInPresenter presenter;

    private InteractionListener interactionListener;

    private ProgressDialog progressDialog;

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    protected SignInPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected SignInPresenter.View getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
        interactionListener = InteractionListener.class.cast(context);
    }

    @Override
    protected void onDetachOverride() {
        super.onDetachOverride();

        interactionListener = null;
    }

    @Nullable
    @Override
    protected View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                    @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCloseSignInView() {
        interactionListener.onCloseSignInView();
    }

    @Override
    public void onStartActivityForResult(@NonNull Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onSnackbar(@StringRes int resId) {
        if (getView() != null) {
            Snackbar.make(getView(), resId, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShowProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.progress_dialog_signin_in));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onHideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    @OnClick(R.id.button_sign_in)
    void onClickButtonSignIn() {
        presenter.onSignIn();
    }

    public interface InteractionListener {

        void onCloseSignInView();
    }
}
