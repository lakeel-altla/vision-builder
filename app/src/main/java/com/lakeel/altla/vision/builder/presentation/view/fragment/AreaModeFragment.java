package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.model.Scope;

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
import android.widget.RadioGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class AreaModeFragment extends Fragment {

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    @BindView(R.id.radio_group_scope)
    RadioGroup radioGroupScope;

    private FragmentContext fragmentContext;

    @NonNull
    public static AreaModeFragment newInstance() {
        return new AreaModeFragment();
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
        final View view = inflater.inflate(R.layout.fragment_area_mode, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(R.string.title_area_mode_view);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        setHasOptionsMenu(true);

        final Scope scope = selectAreaSettingsModel.getAreaScope();
        final int checkedId = (scope == Scope.PUBLIC) ? R.id.radio_button_public : R.id.radio_button_user;
        radioGroupScope.check(checkedId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_area_mode, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                select();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void select() {
        final int checkedId = radioGroupScope.getCheckedRadioButtonId();
        final Scope scope = (checkedId == R.id.radio_button_public) ? Scope.PUBLIC : Scope.USER;
        selectAreaSettingsModel.selectAreaScope(scope);
        fragmentContext.backView();
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void backView();
    }
}
