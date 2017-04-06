package com.lakeel.altla.vision.builder.presentation.view;

import com.lakeel.altla.vision.model.Area;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public interface AreaByPlaceListView {

    void onItemInserted(int position);

    void onItemChanged(int position);

    void onItemRemoved(int position);

    void onItemMoved(int fromPosition, int toPosition);

    void onDataSetChanged();

    void onAreaSelected(@NonNull Area area);

    void onBackView();

    void onCloseView();

    void onSnackbar(@StringRes int resId);
}
