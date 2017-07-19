package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import butterknife.OnClick;

public final class ActorEditMenuPane extends Pane {

    @Inject
    ArModel arModel;

    private final PageContext pageContext;

    public ActorEditMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_actor_edit_menu);

        ((ActivityScopeContext) activity).getActivityComponent().inject(this);
        pageContext = (PageContext) activity;
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        pageContext.closeActorEditMenu();
    }

    @OnClick(R.id.image_button_show_actor_metadata_edit_pane)
    void onClickShowActorMetadataEditPane() {
        pageContext.showActorMetadataEditPane();
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

    public interface PageContext {

        void closeActorEditMenu();

        void showActorMetadataEditPane();
    }
}
