package com.lakeel.altla.android.binding.commandbinder;

import com.lakeel.altla.android.binding.Property;
import com.lakeel.altla.android.binding.propertybinder.DefaultPropertyBinder;
import com.lakeel.altla.android.binding.propertybinder.PropertyBindingDefinition;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;

public final class CompoundButtonCheckedPropertyBinder extends DefaultPropertyBinder {

    private final CompoundButton compoundButton;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public CompoundButtonCheckedPropertyBinder(@NonNull PropertyBindingDefinition definition,
                                               @NonNull View target, @NonNull Property source) {
        super(definition, target, source);

        compoundButton = (CompoundButton) target;
    }

    @Override
    protected void bindSource() {
        onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSource();
            }
        };
        compoundButton.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @Override
    protected void unbindSource() {
        if (onCheckedChangeListener != null) compoundButton.setOnCheckedChangeListener(null);
    }
}
