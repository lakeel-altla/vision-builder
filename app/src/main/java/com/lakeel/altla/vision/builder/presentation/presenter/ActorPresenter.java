package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.property.LongProperty;
import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

public final class ActorPresenter extends BasePresenter<ActorPresenter.View> {

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    @Inject
    ArModel arModel;

    public final StringProperty propertyName = new StringProperty();

    public final LongProperty propertyCreatedAt = new LongProperty(-1);

    public final LongProperty propertyUpdatedAt = new LongProperty(-1);

    public final RelayCommand commandClose = new RelayCommand(this::close);

    @Inject
    public ActorPresenter() {
    }

    @Override
    protected void onResumeOverride() {
        super.onResumeOverride();

        loadActor();
    }

    private void loadActor() {
        final Actor actor = arModel.getSelectedActor();
        if (actor == null) throw new IllegalStateException("No actor is selected.");

        propertyName.set(actor.getName());
        propertyCreatedAt.set(actor.getCreatedAtAsLong());
        propertyUpdatedAt.set(actor.getUpdatedAtAsLong());
    }

    private void close() {
        eventBus.post(ActorViewVisibleEvent.INSTANCE);
    }

    public static final class ActorViewVisibleEvent {

        public static final ActorViewVisibleEvent INSTANCE = new ActorViewVisibleEvent();

        private ActorViewVisibleEvent() {
        }
    }

    public interface View {

    }
}
