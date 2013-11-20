package com.bryankrosenbaum.later;

import android.content.Context;

import com.bryankrosenbaum.later.activity.MainActivity;
import com.bryankrosenbaum.later.activity.ReceiveShareActivity;
import com.bryankrosenbaum.later.data.LaterListSQLiteHelper;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mattmehalso on 11/19/13.
 *
 * DI Module for objects specific to the Later application.  Example - the Later-specific datasource
 * will be provided here.
 *
 */
@Module(
    injects = {MainActivity.class, ReceiveShareActivity.class},
    complete = false
)
public class LaterModule {

    private final Context appContext;

    public LaterModule(Context c) {
        this.appContext = c;
    }

    /**
     * Provider method for the LaterListSqlLiteHelper.  Uses the app context provided when this module
     * is created.
     *
     * @return
     */
    @Provides
    public LaterListSQLiteHelper provideLaterListSqliteHelper() {
        return new LaterListSQLiteHelper(appContext);
    }


}
