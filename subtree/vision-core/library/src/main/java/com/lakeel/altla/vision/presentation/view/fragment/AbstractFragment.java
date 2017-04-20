package com.lakeel.altla.vision.presentation.view.fragment;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.presentation.presenter.Presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractFragment<TView, TPresenter extends Presenter<TView>> extends Fragment {

    private final Log log = LogFactory.getLog(getClass());

    protected abstract TPresenter getPresenter();

    protected abstract TView getViewInterface();

    @Override
    public final void onAttach(Context context) {
        super.onAttach(context);

        onAttachOverride(context);
    }

    protected void onAttachOverride(@NonNull Context context) {
    }

    @Override
    public final void onDetach() {
        super.onDetach();

        onDetachOverride();
    }

    protected void onDetachOverride() {
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPresenter().onCreate(getArguments(), savedInstanceState);

        onCreateOverride(savedInstanceState);
    }

    protected void onCreateOverride(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public final void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getPresenter().onSaveInstanceState(outState);

        onSaveInstanceStateOverride(outState);
    }

    protected void onSaveInstanceStateOverride(Bundle outState) {
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                   @Nullable Bundle savedInstanceState) {
        View view = onCreateViewCore(inflater, container, savedInstanceState);

        if (view != null) {
            onBindView(view);
        }

        getPresenter().onCreateView(getViewInterface());

        onCreateViewOverride(view, savedInstanceState);

        return view;
    }

    @Nullable
    protected abstract View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                             @Nullable Bundle savedInstanceState);

    protected void onBindView(@NonNull View view) {
    }

    protected void onCreateViewOverride(@Nullable View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public final void onStart() {
        super.onStart();

        getPresenter().onStart();

        onStartOverride();
    }

    protected void onStartOverride() {
    }

    @Override
    public final void onStop() {
        super.onStop();

        getPresenter().onStop();

        onStopOverride();
    }

    protected void onStopOverride() {
    }

    @Override
    public final void onResume() {
        super.onResume();

        getPresenter().onResume();

        onResumeOverride();
    }

    protected void onResumeOverride() {
    }

    @Override
    public final void onPause() {
        super.onPause();

        getPresenter().onPause();

        onPauseOverride();
    }

    protected void onPauseOverride() {
    }

    @NonNull
    protected final Log getLog() {
        return log;
    }
}
