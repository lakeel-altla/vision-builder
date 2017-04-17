package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.ConsolePresenter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ConsoleFragment extends AbstractFragment<ConsolePresenter.View, ConsolePresenter>
        implements ConsolePresenter.View {

    private static final String FORMAT_TRANSLATION = "{ %7.2f, %7.2f, %7.2f }";

    @Inject
    ConsolePresenter presenter;

    @BindView(R.id.text_view_localized_value)
    TextView textViewLocalizedValue;

    @BindView(R.id.text_view_ad2ss_translation)
    TextView textViewAd2SsTranslation;

    @BindView(R.id.text_view_ad2d_translation)
    TextView textViewAd2DTranslation;

    @BindView(R.id.text_view_ss2d_translation)
    TextView textViewSs2DTranslation;

    @NonNull
    public static ConsoleFragment newInstance() {
        return new ConsoleFragment();
    }

    @Override
    protected ConsolePresenter getPresenter() {
        return presenter;
    }

    @Override
    protected ConsolePresenter.View getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
    }

    @Nullable
    @Override
    protected android.view.View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_console, container, false);
    }

    @Override
    protected void onBindView(@NonNull android.view.View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onUpdateLocalized(boolean localized) {
        textViewLocalizedValue.setText(Boolean.toString(localized));
    }

    @Override
    public void onUpdateAd2SsTranslation(double x, double y, double z) {
        textViewAd2SsTranslation.setText(String.format(Locale.getDefault(), FORMAT_TRANSLATION, x, y, z));
    }

    @Override
    public void onUpdateAd2DTranslation(double x, double y, double z) {
        textViewAd2DTranslation.setText(String.format(Locale.getDefault(), FORMAT_TRANSLATION, x, y, z));
    }

    @Override
    public void onUpdateSs2DTranslation(double x, double y, double z) {
        textViewSs2DTranslation.setText(String.format(Locale.getDefault(), FORMAT_TRANSLATION, x, y, z));
    }
}
