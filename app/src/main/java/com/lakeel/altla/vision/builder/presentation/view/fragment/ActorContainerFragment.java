package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.ActorContainerPresenter;
import com.lakeel.altla.vision.builder.presentation.view.ActorContainerView;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class ActorContainerFragment extends AbstractFragment<ActorContainerView, ActorContainerPresenter>
        implements ActorContainerView {

    @Inject
    ActorContainerPresenter presenter;

    @NonNull
    public static ActorContainerFragment newInstance() {
        return new ActorContainerFragment();
    }

    @Override
    protected ActorContainerPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected ActorContainerView getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
    }

    @Nullable
    @Override
    protected View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                    @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actor_container, container, false);
    }

    @Override
    public void onShowActorView() {
        replaceFragment(ActorFragment.newInstance());
    }

    private void replaceFragmentAndAddToBackStack(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                                 .addToBackStack(fragment.getClass().getName())
                                 .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                 .commit();
    }

    private void replaceFragment(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                                 .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                 .commit();
    }

    private void backFragment() {
        if (0 < getChildFragmentManager().getBackStackEntryCount()) {
            getChildFragmentManager().popBackStack();
        }
    }
}
