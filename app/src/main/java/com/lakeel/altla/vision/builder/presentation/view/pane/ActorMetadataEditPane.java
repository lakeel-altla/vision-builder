package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.model.Actor;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class ActorMetadataEditPane extends Pane {

    @BindView(R.id.text_input_edit_text_name)
    TextInputEditText textInputEditTextName;

    public ActorMetadataEditPane(@NonNull Activity activity) {
        super(activity, R.id.pane_actor_metadata_edit);
    }

    public void setActor(@Nullable Actor actor) {
        textInputEditTextName.setText(actor == null ? null : actor.getName());
        view.setVisibility(actor == null ? GONE : VISIBLE);
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        view.setVisibility(GONE);
    }
}
