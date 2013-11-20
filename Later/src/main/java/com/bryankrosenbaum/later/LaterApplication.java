package com.bryankrosenbaum.later;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by mattmehalso on 11/19/13.
 */
public class LaterApplication extends Application {
    private ObjectGraph graph;

    @Override public void onCreate() {
        super.onCreate();

        graph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {
        return Arrays.asList(
                new AndroidModule(this),
                new LaterModule(this)
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }
}
