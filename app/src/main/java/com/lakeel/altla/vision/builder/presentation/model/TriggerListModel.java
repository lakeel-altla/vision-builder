package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.model.BoxCollisionComponent;
import com.lakeel.altla.vision.model.CollisionComponent;
import com.lakeel.altla.vision.model.SphereCollisionComponent;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TriggerListModel {

    private final List<Item> items = new ArrayList<>();

    private final Resources resources;

    private int selectedPosition;

    public TriggerListModel(@NonNull Resources resources) {
        this.resources = resources;
        items.add(new BoxCollisionItem());
        items.add(new SphereCollisionItem());
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public Item getItem(int position) {
        return items.get(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public Item getSelectedItem() {
        return (selectedPosition < 0) ? null : getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }

    public interface Item {

        String getName();

        CollisionComponent createCollisionComponent();
    }

    public final class BoxCollisionItem implements Item {

        @Override
        public String getName() {
            return resources.getString(R.string.label_box_collision);
        }

        @Override
        public CollisionComponent createCollisionComponent() {
            final BoxCollisionComponent component = new BoxCollisionComponent();
            component.setUserId(CurrentUser.getInstance().getUserId());
            component.setSize(1, 1, 1);
            return component;
        }
    }

    public final class SphereCollisionItem implements Item {

        @Override
        public String getName() {
            return resources.getString(R.string.label_sphere_collision);
        }

        @Override
        public CollisionComponent createCollisionComponent() {
            final SphereCollisionComponent component = new SphereCollisionComponent();
            component.setRadius(1);
            return component;
        }
    }
}
