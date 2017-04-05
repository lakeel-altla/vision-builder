package com.lakeel.altla.android.binding.propertybinder;

import com.lakeel.altla.android.binding.Property;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public final class EditTextTextPropertyBinder extends DefaultPropertyBinder {

    private final EditText editText;

    private final Property source;

    private TextWatcher textWatcher;

    public EditTextTextPropertyBinder(@NonNull PropertyBindingDefinition definition, @NonNull View target,
                                      @NonNull Property source) {
        super(definition, target, source);

        editText = (EditText) target;
        this.source = source;
    }

    @Override
    protected void updateSourceCore() {
        Editable editable = editText.getText();
        String text = editable == null ? null : editable.toString();
        Object value = getConverter().convertBack(text);
        source.setAsObject(value);
    }

    @Override
    protected void bindSource() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSource();
            }
        };
        editText.addTextChangedListener(textWatcher);
    }

    @Override
    protected void unbindSource() {
        if (textWatcher != null) editText.removeTextChangedListener(textWatcher);
        textWatcher = null;
    }
}
