package com.lakeel.altla.vision.builder.presentation.helper;

import android.support.annotation.NonNull;

import java.util.Arrays;

public final class GridIndex {

    private int[] index;

    public GridIndex(@NonNull int[] index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final GridIndex gridIndex = (GridIndex) obj;
        return Arrays.equals(index, gridIndex.index);
    }

    @Override
    public int hashCode() {
        return index[0] ^ index[1] ^ index[2];
    }
}
