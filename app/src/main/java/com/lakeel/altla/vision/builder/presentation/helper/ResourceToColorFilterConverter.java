package com.lakeel.altla.vision.builder.presentation.helper;

import com.lakeel.altla.android.binding.Converter;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;

public final class ResourceToColorFilterConverter implements Converter {

    private final Resources resources;

    public ResourceToColorFilterConverter(@NonNull Resources resources) {
        this.resources = resources;
    }

    @Override
    public Object convert(Object value) {
        if (value == null) {
            return null;
        }

        int id;
        if (value instanceof Number) {
            id = ((Number) value).intValue();
        } else if (value instanceof String) {
            id = Integer.parseInt((String) value);
        } else {
            throw new IllegalArgumentException(String.format("Type '%s' not supported.", value.getClass()));
        }

        int color = resources.getColor(id);
        return new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public Object convertBack(Object value) {
        throw new UnsupportedOperationException();
    }
}
