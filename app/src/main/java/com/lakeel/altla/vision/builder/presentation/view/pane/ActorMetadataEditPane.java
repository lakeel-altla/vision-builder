package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.model.Actor;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class ActorMetadataEditPane extends Pane {

    @Inject
    ArModel arModel;

    @BindView(R.id.text_input_edit_text_name)
    TextInputEditText textInputEditTextName;

    public ActorMetadataEditPane(@NonNull Activity activity) {
        super(activity, R.id.pane_actor_metadata_edit);
        ((ActivityScopeContext) activity).getActivityComponent().inject(this);
    }

    public void show() {
        final Actor actor = arModel.getSelectedActor();
        if (actor == null) throw new IllegalStateException("No actor is selected.");

        textInputEditTextName.setText(actor.getName());
        view.setVisibility(VISIBLE);
    }

    public void hide() {
        view.setVisibility(GONE);
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        hide();
    }

    @OnClick(R.id.button_save)
    void onClickSave() {
        final Actor actor = arModel.getSelectedActor();
        if (actor == null) throw new IllegalStateException("No actor is selected.");

        actor.setName(textInputEditTextName.getText().toString());

        arModel.saveSelectedActor();

        hide();
    }
}
