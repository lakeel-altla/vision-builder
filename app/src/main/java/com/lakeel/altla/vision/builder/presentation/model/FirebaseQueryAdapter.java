package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.model.BaseEntity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FirebaseQueryAdapter<TItem extends BaseEntity> implements FirebaseQuery.ChildListener<TItem> {

    private static final Log LOG = LogFactory.getLog(FirebaseQueryAdapter.class);

    @NonNull
    private List<TItem> items = new ArrayList<>();

    @Nullable
    private OnItemEventListener onItemEventListener;

    @Override
    public void onChildAdded(@NonNull TItem item, @Nullable String previousChildName) {
        final int position = resolveItemPositionByPreviousId(previousChildName);
        items.add(position, item);
        if (onItemEventListener != null) onItemEventListener.onItemInserted(position);
    }

    @Override
    public void onChildChanged(@NonNull TItem item, String previousChildName) {
        final int position = resolveItemPositionByPreviousId(previousChildName);
        items.set(position, item);
        if (onItemEventListener != null) onItemEventListener.onItemChanged(position);
    }

    @Override
    public void onChildRemoved(@NonNull TItem item) {
        final int position = findItemPositionById(item.getId());
        if (position < 0) {
            LOG.w("An item could not be found: id = %s", item.getId());
        } else {
            items.remove(position);
            if (onItemEventListener != null) onItemEventListener.onItemRemoved(position);
        }
    }

    @Override
    public void onChildMoved(@NonNull TItem item, String previousChildName) {
        final int fromPosition = findItemPositionById(item.getId());
        if (fromPosition < 0) {
            LOG.w("An item could not be found: id = %s", item.getId());
        } else {
            items.remove(fromPosition);
            final int toPosition = resolveItemPositionByPreviousId(previousChildName);
            items.add(toPosition, item);
            if (onItemEventListener != null) onItemEventListener.onItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onError(@NonNull Exception e) {
        LOG.e("Failed to observe the query", e);
    }

    @Nullable
    public OnItemEventListener getOnItemEventListener() {
        return onItemEventListener;
    }

    public void setOnItemEventListener(@Nullable OnItemEventListener onItemEventListener) {
        this.onItemEventListener = onItemEventListener;
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public TItem getItem(int position) {
        return items.get(position);
    }

    public void clear() {
        items.clear();
        if (onItemEventListener != null) onItemEventListener.onDataSetChanged();
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
}
