package com.lakeel.altla.vision.helper;

import com.lakeel.altla.vision.model.AreaDescription;

import java.util.Comparator;

public final class AreaDescriptionNameComparater implements Comparator<AreaDescription> {

    public static final AreaDescriptionNameComparater INSTANCE = new AreaDescriptionNameComparater();

    private AreaDescriptionNameComparater() {
    }

    @Override
    public int compare(AreaDescription o1, AreaDescription o2) {
        if (o1.getName() == null && o2.getName() == null) return 0;
        if (o1.getName() != null && o2.getName() == null) return 1;
        if (o1.getName() == null && o2.getName() != null) return -1;
        return o1.getName().compareTo(o2.getName());
    }
}
