package com.lakeel.altla.vision.presentation.presenter.model;

import com.lakeel.altla.vision.helper.ObservableListEvent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DataList<TItem extends DataList.Item> {

    private final List<TItem> items = new ArrayList<>();

    private final OnItemListener onItemListener;

    public DataList(@NonNull OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public void change(@NonNull ObservableListEvent.Type type, @NonNull TItem item, @Nullable String previousId) {
        switch (type) {
            case ADDED: {
                onAdded(item, previousId);
                break;
            }
            case CHANGED: {
                onChanged(item);
                break;
            }
            case REMOVED: {
                onRemoved(item);
                break;
            }
            case MOVED: {
                onMoved(item, previousId);
                break;
            }
        }
    }

    @NonNull
    public TItem get(int index) {
        return items.get(index);
    }

    public int indexOf(@NonNull String id) {
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            TItem item = items.get(i);
            if (item.getId().equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int size() {
        return items.size();
    }

    public void clear() {
        items.clear();
        onItemListener.onDataSetChanged();
    }

    private void onAdded(@NonNull TItem item, @Nullable String previousId) {
        int index = 0;

        if (previousId != null) {
            int prev = indexOf(previousId);
            index = prev + 1;
        }

        items.add(index, item);
        onItemListener.onItemInserted(index);
    }

    private void onChanged(@NonNull TItem item) {
        int index = indexOf(item.getId());
        items.set(index, item);
        onItemListener.onItemChanged(index);
    }

    private void onRemoved(@NonNull TItem item) {
        int index = indexOf(item.getId());
        items.remove(index);
        onItemListener.onItemRemoved(index);
    }

    private void onMoved(@NonNull TItem item, @Nullable String previousId) {
        int from = indexOf(item.getId());
        items.remove(from);

        int to = 0;
        if (previousId != null) {
            int prev = indexOf(previousId);
            to = prev + 1;
        }
        items.add(to, item);

        onItemListener.onItemMoved(from, to);
    }

    public interface Item {

        String getId();
    }

    public interface OnItemListener {

        void onItemInserted(int index);

        void onItemChanged(int index);

        void onItemRemoved(int index);

        void onItemMoved(int from, int to);

        void onDataSetChanged();
    }
}
