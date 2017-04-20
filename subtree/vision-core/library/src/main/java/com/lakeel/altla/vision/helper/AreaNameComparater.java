package com.lakeel.altla.vision.helper;

import com.lakeel.altla.vision.model.Area;

import java.util.Comparator;

public final class AreaNameComparater implements Comparator<Area> {

    public static final AreaNameComparater INSTANCE = new AreaNameComparater();

    private AreaNameComparater() {
    }

    @Override
    public int compare(Area o1, Area o2) {
        if (o1.getName() == null && o2.getName() == null) return 0;
        if (o1.getName() != null && o2.getName() == null) return 1;
        if (o1.getName() == null && o2.getName() != null) return -1;
        return o1.getName().compareTo(o2.getName());
    }
}
