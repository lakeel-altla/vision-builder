package com.lakeel.altla.vision.builder.presentation.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public final class OnItemEventAdapter implements OnItemEventListener {

    private final RecyclerView.Adapter adapter;

    public OnItemEventAdapter(@NonNull RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemInserted(int position) {
        adapter.notifyItemInserted(position);
    }

    @Override
    public void onItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        adapter.notifyItemMoved(fromPosition, toPosition);
    }
}
