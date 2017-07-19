package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AreaSettingsListModel {

    private static final Log LOG = LogFactory.getLog(AreaSettingsListModel.class);

    private final VisionService visionService;

    private FirebaseQuery<AreaSettings> areaSettingsQuery;

    private List<AreaSettings> items = new ArrayList<>();

    private OnItemEventListener onItemEventListener;

    private int selectedPosition;

    public AreaSettingsListModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @Nullable
    public OnItemEventListener getOnItemEventListener() {
        return onItemEventListener;
    }

    public void setOnItemEventListener(@Nullable OnItemEventListener onItemEventListener) {
        this.onItemEventListener = onItemEventListener;
    }

    @NonNull
    public void loadAllItems() {
        selectedPosition = -1;
        items.clear();
        if (onItemEventListener != null) onItemEventListener.onDataSetChanged();

        areaSettingsQuery = visionService.getUserAreaSettingsApi().findAll();
        areaSettingsQuery.addListener(new FirebaseQuery.ChildListener<AreaSettings>() {
            @Override
            public void onChildAdded(@NonNull AreaSettings areaSettings, @Nullable String previousChildName) {
                final int position = resolveItemPositionByPreviousId(previousChildName);
                items.add(position, areaSettings);
                if (onItemEventListener != null) onItemEventListener.onItemInserted(position);
            }

            @Override
            public void onChildChanged(@NonNull AreaSettings areaSettings, String previousChildName) {
                final int position = resolveItemPositionByPreviousId(previousChildName);
                items.set(position, areaSettings);
                if (onItemEventListener != null) onItemEventListener.onItemChanged(position);
            }

            @Override
            public void onChildRemoved(@NonNull AreaSettings areaSettings) {
                final int position = findItemPositionById(areaSettings.getId());
                if (position < 0) {
                    LOG.w("An item could not be found: id = %s", areaSettings.getId());
                } else {
                    items.remove(position);
                    if (onItemEventListener != null) onItemEventListener.onItemRemoved(position);
                }
            }

            @Override
            public void onChildMoved(@NonNull AreaSettings areaSettings, String previousChildName) {
                final int fromPosition = findItemPositionById(areaSettings.getId());
                if (fromPosition < 0) {
                    LOG.w("An item could not be found: id = %s", areaSettings.getId());
                } else {
                    items.remove(fromPosition);
                    final int toPosition = resolveItemPositionByPreviousId(previousChildName);
                    items.add(toPosition, areaSettings);
                    if (onItemEventListener != null) onItemEventListener.onItemMoved(fromPosition, toPosition);
                }
            }

            @Override
            public void onError(@NonNull Exception e) {
                LOG.e("Failed to observe the area settings list", e);
            }
        });
    }

    @NonNull
    public AreaSettings getItem(int position) {
        return items.get(position);
    }

    public int getItemCount() {
        return items.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public AreaSettings getSelectedItem() {
        return (selectedPosition < 0) ? null : items.get(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }

    private int resolveItemPositionByPreviousId(@Nullable String previousId) {
        if (previousId == null) {
            return 0;
        } else {
            final int previousPosition = findItemPositionById(previousId);
            return previousPosition + 1;
        }
    }

    private int findItemPositionById(@NonNull String id) {
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).getId(), id)) return i;
        }
        return -1;
    }

    public interface OnItemEventListener {

        void onDataSetChanged();

        void onItemInserted(int position);

        void onItemChanged(int position);

        void onItemRemoved(int position);

        void onItemMoved(int fromPosition, int toPosition);
    }
}
