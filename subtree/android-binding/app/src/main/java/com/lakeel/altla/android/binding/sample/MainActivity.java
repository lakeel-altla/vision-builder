package com.lakeel.altla.android.binding.sample;

import com.lakeel.altla.android.binding.BinderFactory;
import com.lakeel.altla.android.binding.Converter;
import com.lakeel.altla.android.binding.annotation.AnnotationBinderFactory;
import com.lakeel.altla.android.binding.annotation.BindProperties;
import com.lakeel.altla.android.binding.annotation.BindProperty;
import com.lakeel.altla.android.binding.annotation.ConverterName;
import com.lakeel.altla.android.binding.annotation.OnClickCommand;
import com.lakeel.altla.android.binding.annotation.OnLongClickCommand;
import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.converter.RelayConverter;
import com.lakeel.altla.android.binding.property.BooleanProperty;
import com.lakeel.altla.android.binding.property.IntProperty;
import com.lakeel.altla.android.binding.property.StringProperty;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public final class MainActivity extends AppCompatActivity {

    private final ViewModel viewModel = new ViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel.onCreate(savedInstanceState);

        AnnotationBinderFactory factory = new AnnotationBinderFactory(new BinderFactory(this));
        factory.create(viewModel).bind();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        viewModel.onSaveInstanceState(outState);
    }

    private final class ViewModel {

        private static final String STATE_RADIO_GROUP_CHECKED = "radioGroupChecked";

        private static final String STATE_TEXT_VIEW_TEXT = "textViewText";

        private static final String STATE_EDIT_TEXT_TEXT = "editTextText";

        private static final String STATE_TOGGLE_BUTTON_CHECKED = "toggleButtonChecked";

        @BindProperty(id = R.id.radio_group_button, name = "checkedButton")
        IntProperty radioGroupChecked;

        @OnClickCommand(R.id.button_on_click)
        final RelayCommand commandClick = new RelayCommand(
                () -> Toast.makeText(MainActivity.this, "onClick", Toast.LENGTH_SHORT).show(),
                () -> radioGroupChecked.get() == R.id.radio_button_button_enabled);

        @BindProperty(id = R.id.text_view_set_text, name = "text")
        StringProperty textViewText;

        @OnClickCommand(R.id.button_set_text)
        final RelayCommand commandTextViewText = new RelayCommand(() -> textViewText.set("Text was set."));

        @OnClickCommand(R.id.button_clear_text)
        final RelayCommand commandClearTextViewText = new RelayCommand(() -> textViewText.set(null));

        @BindProperties({ @BindProperty(id = R.id.edit_text, name = "text"),
                          @BindProperty(id = R.id.text_view_edit_text_result, name = "text") })
        StringProperty editTextText;

        @OnClickCommand(R.id.button_edit_text_clear_text)
        final RelayCommand commandClearEditTextText = new RelayCommand(() -> editTextText.set(null));

        @OnLongClickCommand(R.id.text_view_on_long_click)
        final RelayCommand commandLongClick = new RelayCommand(
                () -> Toast.makeText(MainActivity.this, "onLongClick", Toast.LENGTH_SHORT).show());

        @BindProperties({ @BindProperty(id = R.id.toggle_button, name = "checked"),
                          @BindProperty(id = R.id.text_view_toggle_button_result, name = "text",
                                        converter = "objectStringConverter"),
                          @BindProperty(id = R.id.edit_text_toggle_enabled, name = "enabled") })
        BooleanProperty toggleButtonChecked;

        @ConverterName("objectStringConverter")
        final Converter objectStringConverter = new RelayConverter(value -> value == null ? null : value.toString());

        private ViewModel() {
        }

        void onCreate(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                radioGroupChecked = new IntProperty(R.id.radio_button_button_disabled);
                textViewText = new StringProperty();
                editTextText = new StringProperty();
                toggleButtonChecked = new BooleanProperty();
            } else {
                radioGroupChecked = savedInstanceState.getParcelable(STATE_RADIO_GROUP_CHECKED);
                textViewText = savedInstanceState.getParcelable(STATE_TEXT_VIEW_TEXT);
                editTextText = savedInstanceState.getParcelable(STATE_EDIT_TEXT_TEXT);
                toggleButtonChecked = savedInstanceState.getParcelable(STATE_TOGGLE_BUTTON_CHECKED);
            }

            radioGroupChecked.addOnValueChangedListener(sender -> commandClick.raiseOnCanExecuteChanged());
        }

        void onSaveInstanceState(Bundle outState) {
            outState.putParcelable(STATE_RADIO_GROUP_CHECKED, radioGroupChecked);
            outState.putParcelable(STATE_TEXT_VIEW_TEXT, textViewText);
            outState.putParcelable(STATE_EDIT_TEXT_TEXT, editTextText);
            outState.putParcelable(STATE_TOGGLE_BUTTON_CHECKED, toggleButtonChecked);
        }
    }
}
