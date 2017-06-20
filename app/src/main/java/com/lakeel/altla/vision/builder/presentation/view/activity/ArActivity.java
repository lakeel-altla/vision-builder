package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.android.binding.converter.BooleanToVisibilityConverter;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowSettingsViewEvent;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.builder.presentation.presenter.ArPresenter;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ActorFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ImageAssetListFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.TextureView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public final class ArActivity extends AppCompatActivity implements ActivityScopeContext, ArPresenter.View {

    private static final Log LOG = LogFactory.getLog(ArActivity.class);

    @Inject
    ArPresenter presenter;

    @Inject
    EventBus eventBus;

    @BindView(R.id.view_top)
    ViewGroup viewTop;

    @BindView(R.id.texture_view)
    TextureView textureView;

    @BindView(R.id.image_button_asset_list)
    ImageButton imageButtonAssetList;

    @BindView(R.id.view_group_view_mode_menu)
    ViewGroup viewGroupMainMenu;

    @BindView(R.id.view_group_edit_user_actor_menu)
    ViewGroup viewGroupEditUserActorMenu;

    @BindView(R.id.view_group_translate_menu)
    ViewGroup viewGroupTranslateMenu;

    @BindView(R.id.view_group_rotate_menu)
    ViewGroup viewGroupRotateMenu;

    @BindView(R.id.button_translate)
    Button buttonTranslate;

    @BindViews({ R.id.button_translate_in_x_axis, R.id.button_translate_in_y_axis,
                 R.id.button_translate_in_z_axis })
    Button[] buttonsTranslateAxes;

    @BindView(R.id.button_rotate)
    Button buttonRotate;

    @BindViews({ R.id.button_rotate_in_x_axis, R.id.button_rotate_object_in_y_axis,
                 R.id.button_rotate_object_in_z_axis })
    Button[] buttonsRotateAxes;

    @BindView(R.id.button_scale)
    Button buttonScale;

    @BindView(R.id.button_detail)
    Button buttonEdit;

    private ActivityComponent activityComponent;

    private GestureDetectorCompat gestureDetector;

    private boolean scrolling;

    @NonNull
    public static Intent createIntent(@NonNull Activity activity) {
        return new Intent(activity, ArActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.d("onCreate");

        // NOTE:
        //
        // Any injection must be done before super.onCreate()
        // because fragments are already attached to an activity when they are resumed or instant-run.
        activityComponent = MyApplication.getApplicationComponent(this)
                                         .activityComponent(new ActivityModule(this));
        activityComponent.inject(this);

        eventBus.register(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        presenter.onCreate(getIntent(), savedInstanceState);

        ButterKnife.bind(this, this);

        textureView.setFrameRate(60d);
        textureView.setRenderMode(ISurface.RENDERMODE_WHEN_DIRTY);
        textureView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // returns true to accept a drag event.
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    presenter.onDropModel(event.getClipData());
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }

            return false;
        });

        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                LOG.d("onSingleTapUp");
                return presenter.onSingleTapUp(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                LOG.d("onScroll");
                scrolling = true;
                return presenter.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                LOG.d("onDown");
                // Must return true to receive motion events on onScroll.
                return true;
            }
        });

        textureView.setOnTouchListener((v, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (scrolling) {
                    scrolling = false;
                    presenter.onScrollFinished(event);
                }
            }

            return false;
        });

        // IMPORTANT!!
        //
        // Undesirable gimmick that focuses on the top view explicitly.
        // onKeyEvent/onBackPressed is never called if the view losts the focus.
        viewTop.setFocusable(true);
        viewTop.setFocusableInTouchMode(true);
        viewTop.requestFocus();

        ViewBindingFactory factory = new ViewBindingFactory(viewTop);
        factory.create(R.id.view_group_view_mode_menu, "visibility", presenter.propertyViewModeMenuVisible)
               .converter(BooleanToVisibilityConverter.INSTANCE)
               .bind();
        factory.create(R.id.view_group_edit_mode_menu, "visibility", presenter.propertyEditModeMenuVisible)
               .converter(BooleanToVisibilityConverter.INSTANCE)
               .bind();
        factory.create(R.id.image_button_switch_to_edit_mode, "visibility", presenter.propertySwitchToEditModeVisible)
               .converter(BooleanToVisibilityConverter.INSTANCE)
               .bind();
        factory.create(R.id.image_button_show_settings, "onClick", presenter.commandShowSettings).bind();
        factory.create(R.id.image_button_switch_to_edit_mode, "onClick", presenter.commandSwitchToEditMode).bind();
        factory.create(R.id.image_button_switch_to_view_mode, "onClick", presenter.commandSwitchToViewMode).bind();

        presenter.onCreateView(this);
    }

    @Override
    protected void onDestroy() {
        LOG.d("onDestroy");
        super.onDestroy();

        eventBus.unregister(this);
    }

    @Override
    protected void onStart() {
        LOG.d("onStart");
        super.onStart();

        presenter.onStart();
    }

    @Override
    protected void onStop() {
        LOG.d("onStop");
        super.onStop();

        presenter.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        presenter.onPause();
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void setSurfaceRenderer(ISurfaceRenderer renderer) {
        textureView.setSurfaceRenderer(renderer);
    }

    @Override
    public void resumeTextureView() {
        textureView.onResume();
    }

    @Override
    public void pauseTextureView() {
        textureView.onPause();
    }

    @Override
    public void setMainMenuVisible(boolean visible) {
        viewGroupMainMenu.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onUpdateImageButtonAssetListVisible(boolean visible) {
        imageButtonAssetList.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setActorViewVisible(boolean visible) {
        ActorFragment fragment = findFragment(ActorFragment.class);

        if (visible) {
            if (fragment == null) {
                replaceWindowFragment(ActorFragment.newInstance());
            }
        } else {
            if (fragment != null) {
                removeFragment(fragment);
            }
        }
    }

    @Override
    public void onUpdateAssetListVisible(boolean visible) {
        ImageAssetListFragment fragment = findFragment(ImageAssetListFragment.class);

        if (visible) {
            if (fragment == null) {
                fragment = ImageAssetListFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                                           .replace(R.id.asset_list_container, fragment, fragment.getClass().getName())
                                           .commit();
            }
        } else {
            if (fragment != null) {
                removeFragment(fragment);
            }
        }
    }

    @Override
    public void onUpdateObjectMenuVisible(boolean visible) {
        if (visible) {
            viewGroupEditUserActorMenu.setVisibility(View.VISIBLE);
        } else {
            viewGroupEditUserActorMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUpdateTranslateSelected(boolean selected) {
        buttonTranslate.setPressed(selected);
    }

    @Override
    public void onUpdateTranslateMenuVisible(boolean visible) {
        viewGroupTranslateMenu.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUpdateTranslateAxisSelected(Axis axis, boolean selected) {
        buttonsTranslateAxes[axis.getValue()].setPressed(selected);
    }

    @Override
    public void onUpdateRotateSelected(boolean selected) {
        buttonRotate.setPressed(selected);
    }

    @Override
    public void onUpdateRotateMenuVisible(boolean visible) {
        viewGroupRotateMenu.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUpdateRotateAxisSelected(Axis axis, boolean selected) {
        buttonsRotateAxes[axis.getValue()].setPressed(selected);
    }

    @Override
    public void onUpdateScaleSelected(boolean selected) {
        buttonScale.setPressed(selected);
    }

    @Override
    public void showSnackbar(@StringRes int resId) {
        Snackbar.make(viewTop, resId, Snackbar.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaSettingsViewEvent event) {
        Intent intent = AreaSettingsActivity.createIntent(this);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(@NonNull ShowSettingsViewEvent event) {
        Intent intent = SettingsActivity.createIntent(this);
        startActivity(intent);
    }

    @OnClick(R.id.image_button_area_settings)
    void onClickButtonAreaSettings() {
        presenter.showAreaSettingsView();
    }

    @OnClick(R.id.image_button_asset_list)
    void onClickButtonAssetList() {
        presenter.showAssetListView();
    }

    //
    // NOTE:
    //
    // To keep a button pressed, call setPressed(true) and return true in onTouch event handlers
    // instead of an onClick ones.
    //

    @OnTouch(R.id.button_translate)
    boolean onTouchButtonTranslate(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonTranslate.setPressed(true);
            presenter.onTouchButtonTranslate();
        }
        return true;
    }

    @OnTouch(R.id.button_translate_in_x_axis)
    boolean onTouchButtonTranslateInXAxis(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonsTranslateAxes[Axis.X.getValue()].setPressed(true);
            presenter.onTouchButtonTranslateAxis(Axis.X);
        }
        return true;
    }

    @OnTouch(R.id.button_translate_in_y_axis)
    boolean onTouchButtonTranslateInYAxis(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonsTranslateAxes[Axis.Y.getValue()].setPressed(true);
            presenter.onTouchButtonTranslateAxis(Axis.Y);
        }
        return true;
    }

    @OnTouch(R.id.button_translate_in_z_axis)
    boolean onTouchButtonTranslateInZAxis(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonsTranslateAxes[Axis.Z.getValue()].setPressed(true);
            presenter.onTouchButtonTranslateAxis(Axis.Z);
        }
        return true;
    }

    @OnTouch(R.id.button_rotate)
    boolean onTouchButtonRotateObject(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonRotate.setPressed(true);
            presenter.onTouchButtonRotateObject();
        }
        return true;
    }

    @OnTouch(R.id.button_rotate_in_x_axis)
    boolean onTouchButtonRotateInXAxis(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonsRotateAxes[Axis.X.getValue()].setPressed(true);
            presenter.onTouchButtonRotateAxis(Axis.X);
        }
        return true;
    }

    @OnTouch(R.id.button_rotate_object_in_y_axis)
    boolean onTouchButtonRotateInYAxis(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonsRotateAxes[Axis.Y.getValue()].setPressed(true);
            presenter.onTouchButtonRotateAxis(Axis.Y);
        }
        return true;
    }

    @OnTouch(R.id.button_rotate_object_in_z_axis)
    boolean onTouchButtonRotateInZAxis(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonsRotateAxes[Axis.Z.getValue()].setPressed(true);
            presenter.onTouchButtonRotateAxis(Axis.Z);
        }
        return true;
    }

    @OnTouch(R.id.button_scale)
    boolean onTouchButtonScale(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonScale.setPressed(true);
            presenter.onTouchButtonScale();
        }
        return true;
    }

    @OnTouch(R.id.button_detail)
    boolean onTouchButtonEdit(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonEdit.setPressed(true);
            presenter.onTouchButtonDetail();
        }
        return true;
    }

    @OnClick(R.id.button_delete)
    void onClickButtonDelete() {
        presenter.onClickButtonDelete();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends Fragment> T findFragment(@NonNull Class<T> clazz) {
        return (T) getSupportFragmentManager().findFragmentByTag(clazz.getName());
    }

    private void replaceWindowFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.window_container, fragment, fragment.getClass().getName())
                                   .commit();
    }

    private void removeFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .remove(fragment)
                                   .commit();
    }
}
