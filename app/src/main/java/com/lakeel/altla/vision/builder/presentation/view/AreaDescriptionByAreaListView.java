package com.lakeel.altla.vision.builder.presentation.view;

import com.lakeel.altla.vision.model.AreaDescription;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public interface AreaDescriptionByAreaListView {

    void onItemInserted(int position);

    void onItemChanged(int position);

    void onItemRemoved(int position);

    void onItemMoved(int fromPosition, int toPosition);

    void onDataSetChanged();

    void onAreaDescriptionSelected(@NonNull AreaDescription areaDescription);

    void onCloseView();

    void onSnackbar(@StringRes int resId);
}
