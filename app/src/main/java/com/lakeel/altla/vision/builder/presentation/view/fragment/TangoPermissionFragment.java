package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.TangoPermissionPresenter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class TangoPermissionFragment
        extends AbstractFragment<TangoPermissionPresenter.View, TangoPermissionPresenter>
        implements TangoPermissionPresenter.View {

    @Inject
    TangoPermissionPresenter presenter;

    public static TangoPermissionFragment newInstance() {
        return new TangoPermissionFragment();
    }

    @Override
    protected TangoPermissionPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected TangoPermissionPresenter.View getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ((ActivityScopeContext) context).getActivityComponent().inject(this);
    }

    @Nullable
    @Override
    protected View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                    @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tango_permission, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showAreaLearningPermissionRequiredSnackbar() {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.snackbar_area_learning_permission_required, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_request_permission, view -> presenter.confirmPermission())
                    .show();
        }
    }
}
