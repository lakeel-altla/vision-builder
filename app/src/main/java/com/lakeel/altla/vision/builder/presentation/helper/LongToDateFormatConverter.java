package com.lakeel.altla.vision.builder.presentation.helper;

import com.lakeel.altla.android.binding.Converter;

import android.content.Context;
import android.support.annotation.NonNull;

public final class LongToDateFormatConverter implements Converter {

    private final Context context;

    public LongToDateFormatConverter(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public Object convert(Object value) {
        if (value == null) {
            return null;
        }

        long longValue;
        if (value instanceof Number) {
            longValue = ((Number) value).longValue();
        } else if (value instanceof String) {
            longValue = Long.parseLong((String) value);
        } else {
            throw new IllegalArgumentException(String.format("Type '%s' not supported.", value.getClass()));
        }

        return DateFormatHelper.format(context, longValue);
    }

    @Override
    public Object convertBack(Object value) {
        throw new UnsupportedOperationException();
    }
}
