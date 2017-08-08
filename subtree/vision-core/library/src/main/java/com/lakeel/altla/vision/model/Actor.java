package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class Actor extends BaseEntity {

    public static final String FIELD_NAME = "name";

    public static final String FIELD_COMPONENTS = "components";

    private String name;

    private List<Component> components;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @NonNull
    public List<Component> getComponents() {
        if (components == null) components = new ArrayList<>();
        return components;
    }

    public void setComponents(@Nullable List<Component> components) {
        this.components = components;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Component> T getComponent(@NonNull Class<T> clazz) {
        if (components == null) return null;

        for (final Component component : components) {
            if (clazz.isAssignableFrom(clazz)) {
                return (T) component;
            }
        }
        return null;
    }

    @NonNull
    public <T extends Component> T getRequiredComponent(@NonNull Class<T> clazz) {
        final T component = getComponent(clazz);
        if (component == null) throw new IllegalStateException("The component could not be found: " + clazz);
        return component;
    }

    public void addComponent(@NonNull Component component) {
        getComponents().add(component);
    }

    public void removeComponent(@NonNull Component component) {
        if (components != null) {
            components.remove(component);
            if (components.size() == 0) components = null;
        }
    }

    public void clearComponents() {
        if (components != null) {
            components.clear();
            components = null;
        }
    }
}
