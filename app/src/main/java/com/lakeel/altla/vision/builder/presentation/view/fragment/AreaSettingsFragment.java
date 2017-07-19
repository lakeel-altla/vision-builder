package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.StringResourceHelper;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class AreaSettingsFragment extends Fragment {

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    @BindView(R.id.text_view_area_mode)
    TextView textViewAreaMode;

    @BindView(R.id.text_view_area_name)
    TextView textViewAreaName;

    @BindView(R.id.text_view_area_description_name)
    TextView textViewAreaDescriptionName;

    @BindView(R.id.image_button_show_area_description_list)
    ImageButton imageButtonShowAreaDescriptionList;

    @BindView(R.id.button_start)
    Button buttonStart;

    private FragmentContext fragmentContext;

    @NonNull
    public static AreaSettingsFragment newInstance() {
        return new AreaSettingsFragment();
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
        final View view = inflater.inflate(R.layout.fragment_area_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(R.string.title_area_settings_view);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        setHasOptionsMenu(true);

        textViewAreaMode.setText(StringResourceHelper.resolveScopeStringResource(
                selectAreaSettingsModel.getAreaScope()));
        textViewAreaName.setText(selectAreaSettingsModel.getAreaName());
        textViewAreaDescriptionName.setText(selectAreaSettingsModel.getAreaDescriptionName());

        boolean canShowAreaDescriptionList = (selectAreaSettingsModel.getAreaId() != null);
        int id = canShowAreaDescriptionList ?
                R.color.background_image_button :
                R.color.background_image_button_disabled;
        imageButtonShowAreaDescriptionList.setColorFilter(getResources().getColor(id));
        imageButtonShowAreaDescriptionList.setEnabled(canShowAreaDescriptionList);

        buttonStart.setEnabled(selectAreaSettingsModel.canStart());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_area_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_area_settings_list_view:
                fragmentContext.showAreaSettingsListView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.image_button_area_mode)
    void onClickAreaMode() {
        fragmentContext.showAreaModeView();
    }

    @OnClick(R.id.image_button_area_find)
    void onClickAreaFind() {
        fragmentContext.showAreaFindView();
    }

    @OnClick(R.id.image_button_show_area_description_list)
    void onClickAreaDescriptionList() {
        fragmentContext.showAreaDescriptionByAreaListView();
    }

    @OnClick(R.id.button_start)
    void onClickStart() {
        if (selectAreaSettingsModel.canStart()) {
            selectAreaSettingsModel.start();
            fragmentContext.backView();
        }
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void backView();

        void showAreaModeView();

        void showAreaFindView();

        void showAreaDescriptionByAreaListView();

        void showAreaSettingsListView();
    }
}
