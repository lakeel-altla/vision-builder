package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.ActorContainerPresenter;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class ActorContainerFragment
        extends AbstractFragment<ActorContainerPresenter.View, ActorContainerPresenter>
        implements ActorContainerPresenter.View {

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
    protected ActorContainerPresenter.View getViewInterface() {
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
        return inflater.inflate(R.layout.fragment_actor_container, container, false);
    }

    @Override
    protected void onCreateViewOverride(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onCreateViewOverride(view, savedInstanceState);

        if (savedInstanceState == null) {
            addFragment(ActorFragment.newInstance());
        }
    }

    @Override
    public void showActorEditView(@NonNull Actor actor) {
        addFragment(ActorEditFragment.newInstance(actor));
    }

    @Override
    public void backView() {
        backFragment();
    }

    private void addFragment(@NonNull Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        Fragment topFragment = getTopFragment();
        if (topFragment != null) transaction.hide(topFragment);

        transaction.addToBackStack(fragment.getClass().getName())
                   .add(R.id.fragment_container, fragment, fragment.getClass().getName())
                   .commit();
    }

    private void backFragment() {
        if (0 < getChildFragmentManager().getBackStackEntryCount()) {
            getChildFragmentManager().popBackStack();
        }
    }

    @Nullable
    private Fragment getTopFragment() {
        FragmentManager manager = getChildFragmentManager();

        int index = manager.getBackStackEntryCount() - 1;
        if (index < 0) {
            return null;
        } else {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(index);
            return manager.findFragmentByTag(entry.getName());
        }
    }
}
