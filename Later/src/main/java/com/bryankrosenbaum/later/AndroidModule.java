package com.bryankrosenbaum.later;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mattmehalso on 11/19/13.
 *
 * DI Module for Android classes that require a Context, Application,
 * or other Android-specific injected object.  Providers specific to this application should <i>not</i>
 * go in here, and instead should be placed in LaterModule.
 */
@Module(library = true)
public class AndroidModule {

    private final LaterApplication application;

    /**
     * Set the application singleton to the module on construction so that we can always provide an
     * app context.
     * @param app
     */
    public AndroidModule(LaterApplication app) {
        this.application = app;
    }

    @Provides @Singleton
    public Context provideApplicationContext() {
        return application;
    }


}
