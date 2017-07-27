package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class DebugMenuPane extends Pane {

    @BindView(R.id.image_button_expand)
    ImageButton imageButtonEnpand;

    @BindView(R.id.view_group_options)
    ViewGroup viewGroupOptions;

    @BindView(R.id.button_set_frame_buffers_visible)
    Button buttonSetFrameBuffersVisible;

    @BindView(R.id.button_set_tango_meshes_visible)
    Button buttonSetTangoMeshesVisible;

    private final PaneContext paneContext;

    public DebugMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_debug_menu);
        paneContext = (PaneContext) activity;
        imageButtonEnpand.setSelected(false);
        viewGroupOptions.setVisibility(GONE);
    }

    @OnClick(R.id.image_button_expand)
    void onClickExpand() {
        final boolean selected = !imageButtonEnpand.isSelected();
        imageButtonEnpand.setSelected(selected);
        viewGroupOptions.setVisibility(selected ? VISIBLE : GONE);
    }

    @OnClick(R.id.button_set_frame_buffers_visible)
    void onClickSetFrameBuffersVisible() {
        final boolean selected = !buttonSetFrameBuffersVisible.isSelected();
        buttonSetFrameBuffersVisible.setSelected(selected);
        final int resId = selected ?
                R.string.button_set_frame_buffers_visible_false :
                R.string.button_set_frame_buffers_visible_true;
        buttonSetFrameBuffersVisible.setText(resId);
        paneContext.setDebugFrameBuffersVisible(selected);
    }

    @OnClick(R.id.button_set_tango_meshes_visible)
    void onClickSetTangoMeshesVisible() {
        final boolean selected = !buttonSetTangoMeshesVisible.isSelected();
        buttonSetTangoMeshesVisible.setSelected(selected);
        final int resId = selected ?
                R.string.button_set_tango_meshes_visible_false :
                R.string.button_set_tango_meshes_visible_true;
        buttonSetTangoMeshesVisible.setText(resId);
        paneContext.setDebugTangoMeshesVisible(selected);
    }

    public interface PaneContext {

        void setDebugFrameBuffersVisible(boolean visible);

        void setDebugTangoMeshesVisible(boolean visible);
    }
}
