package com.lakeel.altla.vision.helper;

import com.lakeel.altla.vision.model.Component;

import android.support.annotation.NonNull;

public interface ComponentTypeResolver {

    Class<? extends Component> resolve(@NonNull String type);
}
