package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.model.Actor;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class ActorPane extends Pane {

    @BindView(R.id.text_view_name)
    TextView textViewName;

    public ActorPane(@NonNull Activity activity) {
        super(activity, R.id.pane_actor);
    }

    public void setActor(@Nullable Actor actor) {
        textViewName.setText(actor == null ? null : actor.getName());
        view.setVisibility(actor == null ? GONE : VISIBLE);
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        view.setVisibility(GONE);
    }
}
