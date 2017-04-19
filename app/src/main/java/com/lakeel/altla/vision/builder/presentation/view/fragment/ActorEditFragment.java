package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.ActorEditPresenter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public final class ActorEditFragment extends AbstractFragment<ActorEditPresenter.View, ActorEditPresenter>
        implements ActorEditPresenter.View {

    @Inject
    ActorEditPresenter presenter;

    @BindView(R.id.view_top)
    android.view.View viewTop;

    @BindView(R.id.text_input_layout_name)
    TextInputLayout textInputLayoutName;

    @BindView(R.id.text_input_edit_text_name)
    TextInputEditText textInputEditTextName;

    @BindView(R.id.edit_text_position_x)
    EditText editTextPositionX;

    @BindView(R.id.edit_text_position_y)
    EditText editTextPositionY;

    @BindView(R.id.edit_text_position_z)
    EditText editTextPositionZ;

    @BindView(R.id.edit_text_orientation_x)
    EditText editTextOrientationX;

    @BindView(R.id.edit_text_orientation_y)
    EditText editTextOrientationY;

    @BindView(R.id.edit_text_orientation_z)
    EditText editTextOrientationZ;

    @BindView(R.id.edit_text_orientation_w)
    EditText editTextOrientationW;

    @BindView(R.id.edit_text_scale_x)
    EditText editTextScaleX;

    @BindView(R.id.edit_text_scale_y)
    EditText editTextScaleY;

    @BindView(R.id.edit_text_scale_z)
    EditText editTextScaleZ;

    private InteractionListener interactionListener;

    private boolean actionSaveEnabled;

    @NonNull
    public static ActorEditFragment newInstance(@NonNull String sceneId, @NonNull String actorId) {
        ActorEditFragment fragment = new ActorEditFragment();
        Bundle bundle = ActorEditPresenter.createArguments(sceneId, actorId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected ActorEditPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected ActorEditPresenter.View getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        interactionListener = InteractionListener.class.cast(context);
        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
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
        return inflater.inflate(R.layout.fragment_actor_edit, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_actor_edit, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_save).setVisible(actionSaveEnabled);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                presenter.onActionSave();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onUpdateViewsEnabled(boolean enabled) {
        editTextPositionX.setEnabled(enabled);
        editTextPositionY.setEnabled(enabled);
        editTextPositionZ.setEnabled(enabled);
        editTextOrientationX.setEnabled(enabled);
        editTextOrientationY.setEnabled(enabled);
        editTextOrientationZ.setEnabled(enabled);
        editTextOrientationW.setEnabled(enabled);
        editTextScaleX.setEnabled(enabled);
        editTextScaleY.setEnabled(enabled);
        editTextScaleZ.setEnabled(enabled);
    }

    @Override
    public void onUpdateActionSave(boolean enabled) {
        actionSaveEnabled = enabled;
        interactionListener.onInvalidateOptionsMenu();
    }

    @Override
    public void onUpdateHomeAsUpIndicator(@DrawableRes int resId) {
        ActionBar actionBar = AppCompatActivity.class.cast(getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(resId);
        }
    }

    @Override
    public void onUpdateHomeAsUpIndicator(@Nullable Drawable drawable) {
        ActionBar actionBar = AppCompatActivity.class.cast(getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
        }
    }

    @Override
    public void onUpdateTitle(@Nullable String title) {
        getActivity().setTitle(title);
    }

    @Override
    public void onUpdateName(@Nullable String name) {
        textInputEditTextName.setText(name);
    }

    @Override
    public void onUpdatePositionX(double x) {
        editTextPositionX.setText(String.valueOf(x));
    }

    @Override
    public void onUpdatePositionY(double y) {
        editTextPositionY.setText(String.valueOf(y));
    }

    @Override
    public void onUpdatePositionZ(double z) {
        editTextPositionZ.setText(String.valueOf(z));
    }

    @Override
    public void onUpdateOrientationX(double x) {
        editTextOrientationX.setText(String.valueOf(x));
    }

    @Override
    public void onUpdateOrientationY(double y) {
        editTextOrientationY.setText(String.valueOf(y));
    }

    @Override
    public void onUpdateOrientationZ(double z) {
        editTextOrientationZ.setText(String.valueOf(z));
    }

    @Override
    public void onUpdateOrientationW(double w) {
        editTextOrientationW.setText(String.valueOf(w));
    }

    @Override
    public void onUpdateScaleX(double x) {
        editTextScaleX.setText(String.valueOf(x));
    }

    @Override
    public void onUpdateScaleY(double y) {
        editTextScaleY.setText(String.valueOf(y));
    }

    @Override
    public void onUpdateScaleZ(double z) {
        editTextScaleZ.setText(String.valueOf(z));
    }

    @Override
    public void onShowNameError(@StringRes int resId) {
        textInputLayoutName.setError(getString(resId));
    }

    @Override
    public void onHideNameError() {
        textInputLayoutName.setError(null);
    }

    @Override
    public void onBackView() {
        interactionListener.onBackView();
    }

    @OnTextChanged(value = R.id.text_input_edit_text_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextNameAfterTextChanged(Editable editable) {
        presenter.onEditTextNameAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_position_x, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextPositionXAfterTextChanged(Editable editable) {
        presenter.onEditTextPositionXAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_position_y, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextPositionYAfterTextChanged(Editable editable) {
        presenter.onEditTextPositionYAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_position_z, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextPositionZAfterTextChanged(Editable editable) {
        presenter.onEditTextPositionZAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_orientation_x, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextOrientationXAfterTextChanged(Editable editable) {
        presenter.onEditTextOrientationXAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_orientation_y, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextOrientationYAfterTextChanged(Editable editable) {
        presenter.onEditTextOrientationYAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_orientation_z, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextOrientationZAfterTextChanged(Editable editable) {
        presenter.onEditTextOrientationZAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_orientation_w, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextOrientationWAfterTextChanged(Editable editable) {
        presenter.onEditTextOrientationWAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_scale_x, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextScaleXAfterTextChanged(Editable editable) {
        presenter.onEditTextScaleXAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_scale_y, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextScaleYAfterTextChanged(Editable editable) {
        presenter.onEditTextScaleYAfterTextChanged(editable.toString());
    }

    @OnTextChanged(value = R.id.edit_text_scale_z, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onEditTextScaleZAfterTextChanged(Editable editable) {
        presenter.onEditTextScaleZAfterTextChanged(editable.toString());
    }

    public interface InteractionListener {

        void onInvalidateOptionsMenu();

        void onBackView();
    }
}
