package com.lakeel.altla.android.binding.propertybinder;

import com.lakeel.altla.android.binding.Property;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioGroup;

public final class RadioGroupCheckedPropertyBinder extends DefaultPropertyBinder {

    private final RadioGroup radioGroup;

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener;

    public RadioGroupCheckedPropertyBinder(@NonNull PropertyBindingDefinition definition, @NonNull View target,
                                           @NonNull Property source) {
        super(definition, target, source);

        radioGroup = (RadioGroup) target;
    }

    @Override
    protected void bindSource() {
        onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSource();
            }
        };
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @Override
    protected void unbindSource() {
        radioGroup.setOnCheckedChangeListener(null);
        onCheckedChangeListener = null;
    }
}
