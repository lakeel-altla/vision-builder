package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.google.firebase.auth.FirebaseAuth;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class SettingsFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(SettingsFragment.class);

    @Inject
    VisionService visionService;

    @BindView(R.id.button_sign_out)
    Button buttonSignOut;

    private FragmentContext fragmentContext;

    @NonNull
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
        fragmentContext = (FragmentContext) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.button_sign_out)
    void onClickSignOut() {
        visionService.getUserDeviceConnectionApi()
                     .markUserDeviceConnectionAsOffline(aVoid -> {
                         showSignInView();
                     }, e -> {
                         LOG.e("Failed.", e);
                         showSignInView();
                     });
    }

    private void showSignInView() {
        FirebaseAuth.getInstance().signOut();
        fragmentContext.showSignInView();
    }

    public interface FragmentContext {

        void showSignInView();
    }
}
