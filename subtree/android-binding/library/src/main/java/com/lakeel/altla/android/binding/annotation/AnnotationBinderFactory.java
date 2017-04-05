package com.lakeel.altla.android.binding.annotation;

import com.lakeel.altla.android.binding.BinderFactory;

import android.support.annotation.NonNull;

public final class AnnotationBinderFactory {

    private final BinderFactory binderFactory;

    public AnnotationBinderFactory(@NonNull BinderFactory binderFactory) {
        this.binderFactory = binderFactory;
    }

    @NonNull
    public AnnotationBinder create(@NonNull Object object) {
        return new DefaultAnnotationBinder(binderFactory).create(object);
    }
}
