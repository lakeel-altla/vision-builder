package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.builder.presentation.model.Axis;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class ActorEditMenuPane extends Pane {

    @Inject
    ArModel arModel;

    @BindViews({ R.id.image_button_show_actor_metadata_edit_view, R.id.image_button_delete })
    ImageButton[] buttonsDisabledIfTranslateMenuSelected;

    private final PaneContext paneContext;

    private TransformMenu transformMenu;

    public ActorEditMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_actor_edit_menu);

        ((ActivityScopeContext) activity).getActivityComponent().inject(this);
        paneContext = (PaneContext) activity;

        transformMenu = new TransformMenu();
        transformMenu.setSelected(false);
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        transformMenu.setSelected(false);
        paneContext.closeActorEditMenu();
    }

    @OnClick(R.id.image_button_show_actor_metadata_edit_view)
    void onClickShowActorMetadataEditPane() {
        paneContext.showActorMetadataEditView();
    }

    @OnClick(R.id.image_button_delete)
    void onClickDelete() {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.dialog_message_confirm_delete)
                .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                })
                .setPositiveButton(R.string.dialog_button_delete, (dialog, which) -> {
                    arModel.deleteSelectedActor();
                })
                .show();
    }

    public interface PaneContext {

        void closeActorEditMenu();

        void showActorMetadataEditView();

        void setSelectedActorLocked(boolean locked);

        void setTranslationEnabled(boolean enabled, @Nullable Axis axis);

        void setRotationEnabled(boolean enabled, @Nullable Axis axis);

        void setScaleEnabled(boolean enabled);
    }

    class TransformMenu {

        @BindView(R.id.image_button_transform_menu)
        ImageButton imageButtonMenu;

        TranslateMenu translateMenu;

        RotateMenu rotateMenu;

        ScaleMenu scaleMenu;

        TransformMenu() {
            ButterKnife.bind(this, view);

            translateMenu = new TranslateMenu();
            rotateMenu = new RotateMenu();
            scaleMenu = new ScaleMenu();
        }

        @OnClick(R.id.image_button_transform_menu)
        void onClickMenu() {
            setSelected(!imageButtonMenu.isSelected());
        }

        void setSelected(boolean selected) {
            imageButtonMenu.setSelected(selected);
            translateMenu.setVisible(selected);
            rotateMenu.setVisible(selected);
            scaleMenu.setVisible(selected);

            for (final ImageButton button : buttonsDisabledIfTranslateMenuSelected) {
                button.setEnabled(!selected);
            }

            paneContext.setSelectedActorLocked(selected);
        }
    }

    class TranslateMenu {

        @BindView(R.id.button_translate_menu)
        Button buttonMenu;

        @BindView(R.id.button_translate_x_axis)
        Button buttonXAxis;

        @BindView(R.id.button_translate_y_axis)
        Button buttonYAxis;

        @BindView(R.id.button_translate_z_axis)
        Button buttonZAxis;

        @BindViews({ R.id.button_translate_x_axis, R.id.button_translate_y_axis, R.id.button_translate_z_axis })
        List<Button> buttonOptions;

        TranslateMenu() {
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.button_translate_menu)
        void onClickMenu() {
            setSelected(!buttonMenu.isSelected());
        }

        @OnClick(R.id.button_translate_x_axis)
        void onClickXAxis() {
            resetOptions();
            buttonXAxis.setSelected(true);
            paneContext.setTranslationEnabled(true, Axis.X);
        }

        @OnClick(R.id.button_translate_y_axis)
        void onClickYAxis() {
            resetOptions();
            buttonYAxis.setSelected(true);
            paneContext.setTranslationEnabled(true, Axis.Y);
        }

        @OnClick(R.id.button_translate_z_axis)
        void onClickZAxis() {
            resetOptions();
            buttonZAxis.setSelected(true);
            paneContext.setTranslationEnabled(true, Axis.Z);
        }

        void setSelected(boolean selected) {
            buttonMenu.setSelected(selected);
            for (final Button button : buttonOptions) {
                button.setVisibility(selected ? VISIBLE : GONE);
                if (!selected) {
                    button.setSelected(false);
                }
            }

            if (selected) {
                transformMenu.rotateMenu.setSelected(false);
                transformMenu.scaleMenu.setSelected(false);
            } else {
                paneContext.setTranslationEnabled(false, null);
            }
        }

        void setVisible(boolean visible) {
            buttonMenu.setVisibility(visible ? VISIBLE : GONE);
            if (!visible) {
                setSelected(false);
            }
        }

        void resetOptions() {
            for (final Button button : buttonOptions) {
                button.setSelected(false);
            }
        }
    }

    class RotateMenu {

        @BindView(R.id.button_rotate_menu)
        Button buttonMenu;

        @BindView(R.id.button_rotate_x_axis)
        Button buttonXAxis;

        @BindView(R.id.button_rotate_y_axis)
        Button buttonYAxis;

        @BindView(R.id.button_rotate_z_axis)
        Button buttonZAxis;

        @BindViews({ R.id.button_rotate_x_axis, R.id.button_rotate_y_axis, R.id.button_rotate_z_axis })
        List<Button> buttonOptions;

        RotateMenu() {
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.button_rotate_menu)
        void onClickMenu() {
            setSelected(!buttonMenu.isSelected());
        }

        @OnClick(R.id.button_rotate_x_axis)
        void onClickXAxis() {
            resetOptions();
            buttonXAxis.setSelected(true);
            paneContext.setRotationEnabled(true, Axis.X);
        }

        @OnClick(R.id.button_rotate_y_axis)
        void onClickYAxis() {
            resetOptions();
            buttonYAxis.setSelected(true);
            paneContext.setRotationEnabled(true, Axis.Y);
        }

        @OnClick(R.id.button_rotate_z_axis)
        void onClickZAxis() {
            resetOptions();
            buttonZAxis.setSelected(true);
            paneContext.setRotationEnabled(true, Axis.Z);
        }

        void setSelected(boolean selected) {
            buttonMenu.setSelected(selected);
            for (final Button button : buttonOptions) {
                button.setVisibility(selected ? VISIBLE : GONE);
                if (!selected) {
                    button.setSelected(false);
                }
            }

            if (selected) {
                transformMenu.translateMenu.setSelected(false);
                transformMenu.scaleMenu.setSelected(false);
            } else {
                paneContext.setRotationEnabled(false, null);
            }
        }

        void setVisible(boolean visible) {
            buttonMenu.setVisibility(visible ? VISIBLE : GONE);
            if (!visible) {
                setSelected(false);
            }
        }

        void resetOptions() {
            for (final Button button : buttonOptions) {
                button.setSelected(false);
            }
        }
    }

    class ScaleMenu {

        @BindView(R.id.button_scale_menu)
        Button buttonMenu;

        ScaleMenu() {
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.button_scale_menu)
        void onClickMenu() {
            setSelected(!buttonMenu.isSelected());
            if (buttonMenu.isSelected()) {
                transformMenu.translateMenu.setSelected(false);
                transformMenu.rotateMenu.setSelected(false);
            }
        }

        void setSelected(boolean selected) {
            buttonMenu.setSelected(selected);
            paneContext.setScaleEnabled(selected);
        }

        void setVisible(boolean visible) {
            buttonMenu.setVisibility(visible ? VISIBLE : GONE);
            if (!visible) {
                setSelected(false);
            }
        }
    }
}
