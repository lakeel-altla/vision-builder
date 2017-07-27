package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class DebugMenuPane extends Pane {

    @BindView(R.id.image_button_expand)
    ImageButton imageButtonEnpand;

    @BindView(R.id.image_button_collapse)
    ImageButton imageButtonCollapse;

    @BindViews({ R.id.button_set_frame_buffers_visible, R.id.button_set_tango_meshes_visible })
    List<View> optionViews;

    @BindView(R.id.button_set_frame_buffers_visible)
    Button buttonSetFrameBuffersVisible;

    @BindView(R.id.button_set_tango_meshes_visible)
    Button buttonSetTangoMeshesVisible;

    private final PaneContext paneContext;

    private boolean debugFrameBuffersVisible;

    private boolean debugTangoMeshesVisible;

    public DebugMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_debug_menu);
        paneContext = (PaneContext) activity;
        collapse();
    }

    public void expand() {
        imageButtonEnpand.setVisibility(GONE);
        imageButtonCollapse.setVisibility(VISIBLE);
        for (final View view : optionViews) {
            view.setVisibility(VISIBLE);
        }
    }

    public void collapse() {
        imageButtonEnpand.setVisibility(VISIBLE);
        imageButtonCollapse.setVisibility(GONE);
        for (final View view : optionViews) {
            view.setVisibility(GONE);
        }
    }

    @OnClick(R.id.image_button_expand)
    void onClickExpand() {
        expand();
    }

    @OnClick(R.id.image_button_collapse)
    void onClickCollapse() {
        collapse();
    }

    @OnClick(R.id.button_set_frame_buffers_visible)
    void onClickSetFrameBuffersVisible() {
        debugFrameBuffersVisible = !debugFrameBuffersVisible;
        final int resId = debugFrameBuffersVisible ?
                R.string.button_set_frame_buffers_visible_false :
                R.string.button_set_frame_buffers_visible_true;
        buttonSetFrameBuffersVisible.setText(resId);
        paneContext.setDebugFrameBuffersVisible(debugFrameBuffersVisible);
    }

    @OnClick(R.id.button_set_tango_meshes_visible)
    void onClickSetTangoMeshesVisible() {
        debugTangoMeshesVisible = !debugTangoMeshesVisible;
        final int resId = debugTangoMeshesVisible ?
                R.string.button_set_tango_meshes_visible_false :
                R.string.button_set_tango_meshes_visible_true;
        buttonSetTangoMeshesVisible.setText(resId);
        paneContext.setDebugTangoMeshesVisible(debugTangoMeshesVisible);
    }

    public interface PaneContext {

        void setDebugFrameBuffersVisible(boolean visible);

        void setDebugTangoMeshesVisible(boolean visible);
    }
}
