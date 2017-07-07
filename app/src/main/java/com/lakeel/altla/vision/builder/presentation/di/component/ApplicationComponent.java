package com.lakeel.altla.vision.builder.presentation.di.component;

import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.di.module.ApplicationModule;
import com.lakeel.altla.vision.builder.presentation.di.module.FirebaseModule;
import com.lakeel.altla.vision.builder.presentation.di.module.Names;

import android.content.Context;
import android.content.res.Resources;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class,
                       FirebaseModule.class })
public interface ApplicationComponent {

    ActivityComponent activityComponent(ActivityModule module);

    @Named(Names.APPLICATION_CONTEXT)
    Context context();

    Resources resources();
}
