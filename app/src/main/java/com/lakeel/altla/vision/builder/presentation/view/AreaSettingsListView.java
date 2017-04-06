package com.lakeel.altla.vision.builder.presentation.view;

import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public interface AreaSettingsListView {

    void onItemInserted(int position);

    void onDataSetChanged();

    void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                @NonNull Area area,
                                @NonNull AreaDescription areaDescription);

    void onCloseView();

    void onSnackbar(@StringRes int resId);
}
