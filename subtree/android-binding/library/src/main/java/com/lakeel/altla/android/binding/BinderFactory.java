package com.lakeel.altla.android.binding;

import com.lakeel.altla.android.binding.commandbinder.CommandBindingDefinition;
import com.lakeel.altla.android.binding.commandbinder.CommandBindingDefinitionRegistry;
import com.lakeel.altla.android.binding.commandbinder.CompoundButtonCheckedPropertyBinder;
import com.lakeel.altla.android.binding.commandbinder.ViewOnClickCommandBinder;
import com.lakeel.altla.android.binding.commandbinder.ViewOnLongClickCommandBinder;
import com.lakeel.altla.android.binding.propertybinder.DefaultPropertyBinder;
import com.lakeel.altla.android.binding.propertybinder.EditTextTextPropertyBinder;
import com.lakeel.altla.android.binding.propertybinder.PropertyBindingDefinition;
import com.lakeel.altla.android.binding.propertybinder.PropertyBindingDefinitionRegistry;
import com.lakeel.altla.android.binding.propertybinder.RadioGroupCheckedPropertyBinder;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class BinderFactory {

    private static final String TAG = BinderFactory.class.getSimpleName();

    private final PropertyBindingDefinitionRegistry propertyBindingDefinitionRegistry =
            new PropertyBindingDefinitionRegistry();

    private final CommandBindingDefinitionRegistry commandBindingDefinitionRegistry =
            new CommandBindingDefinitionRegistry();

    private final Activity activity;

    private final View container;

    public BinderFactory(@NonNull Activity activity) {
        this(activity, null);
    }

    public BinderFactory(@NonNull View container) {
        this(null, container);
    }

    private BinderFactory(Activity activity, View container) {
        this.activity = activity;
        this.container = container;

        try {
            propertyBindingDefinitionRegistry.register(PropertyBindingDefinition.create(
                    EditText.class, "text", CharSequence.class,
                    BindingMode.TWO_WAY, EditTextTextPropertyBinder.class
            ));
            propertyBindingDefinitionRegistry.register(PropertyBindingDefinition.create(
                    CompoundButton.class, "checked",
                    BindingMode.TWO_WAY, CompoundButtonCheckedPropertyBinder.class
            ));
            propertyBindingDefinitionRegistry.register(PropertyBindingDefinition.create(
                    RadioGroup.class, "checkedButton", int.class, "getCheckedRadioButtonId", "check",
                    BindingMode.TWO_WAY, RadioGroupCheckedPropertyBinder.class
            ));

            commandBindingDefinitionRegistry.register(new CommandBindingDefinition(
                    View.class, "onClick", ViewOnClickCommandBinder.class
            ));
            commandBindingDefinitionRegistry.register(new CommandBindingDefinition(
                    View.class, "onLongClick", ViewOnLongClickCommandBinder.class
            ));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public PropertyBindingDefinitionRegistry getPropertyBindingDefinitionRegistry() {
        return propertyBindingDefinitionRegistry;
    }

    @NonNull
    public CommandBindingDefinitionRegistry getCommandBindingDefinitionRegistry() {
        return commandBindingDefinitionRegistry;
    }

    @NonNull
    public <TView extends View> PropertyBinder create(@NonNull TView target, @NonNull String propertyName,
                                                      @NonNull Property source) {
        Class<? extends View> viewType = target.getClass();

        PropertyBindingDefinition definition = propertyBindingDefinitionRegistry.find(viewType, propertyName);
        if (definition == null) {
            Log.d(TAG, String.format("The default definition will be used: viewType = %s, propertyName = %s",
                                     viewType, propertyName));
            try {
                definition = PropertyBindingDefinition.create(
                        viewType, propertyName, BindingMode.ONE_WAY, DefaultPropertyBinder.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Failed to create the definition.", e);
                throw new RuntimeException(e);
            }
        }

        Constructor<? extends PropertyBinder> constructor = definition.getBinderConstructor();
        try {
            return constructor.newInstance(definition, target, source);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public <TView extends View> CommandBinder create(@NonNull TView target, @NonNull String commandName,
                                                     @NonNull Command source) {
        Class<? extends View> viewType = target.getClass();

        CommandBindingDefinition definition = commandBindingDefinitionRegistry.find(viewType, commandName);
        if (definition == null) {
            throw new IllegalArgumentException(
                    String.format("No such definition exists: viewType = %s, commandName = %s",
                                  viewType, commandName));

        }

        Constructor<? extends CommandBinder> constructor = definition.getBinderConstructor();
        try {
            return constructor.newInstance(definition, target, source);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public <TView extends View> PropertyBinder create(@IdRes int id, @NonNull String propertyName,
                                                      @NonNull Property source) {
        TView target = findById(id);
        return create(target, propertyName, source);
    }

    @NonNull
    public <TView extends View> CommandBinder create(@IdRes int id, @NonNull String commandName,
                                                     @NonNull Command source) {
        TView target = findById(id);
        return create(target, commandName, source);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T extends View> T findById(@IdRes int id) {
        T view = null;

        if (activity != null) {
            view = (T) activity.findViewById(id);
        } else if (container != null) {
            view = (T) container.findViewById(id);
        }

        if (view == null) {
            throw new IllegalStateException("No id container exists.");
        }

        return view;
    }

    private final class CompositeUnbindable implements Unbindable {

        private final List<Unbindable> unbindables = new ArrayList<>();

        public void add(@NonNull Unbindable unbindable) {
            unbindables.add(unbindable);
        }

        @Override
        public void unbind() {
            for (Unbindable unbindable : unbindables) {
                unbindable.unbind();
            }
        }
    }
}
