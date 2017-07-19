package com.lakeel.altla.vision.builder.presentation.model;

public interface OnItemEventListener {

    void onDataSetChanged();

    void onItemInserted(int position);

    void onItemChanged(int position);

    void onItemRemoved(int position);

    void onItemMoved(int fromPosition, int toPosition);
}
