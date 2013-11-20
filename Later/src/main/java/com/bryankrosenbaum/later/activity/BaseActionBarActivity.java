package com.bryankrosenbaum.later.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.bryankrosenbaum.later.LaterApplication;

/**
 * Base activity that all others should extend from so that injection is set up on creation. This way
 * we don't have to worry about ugly DI-specific code mucking up our application's activity classes.
 * Similar to the RoboActivity provided by RoboGuice; Dagger doesn't provide an equivalent so we need
 * to create one here.  There are some add-on libraries that do all this for you, btw, but wanted to
 * learn how all this works.
 *
 * BaseActionBarActivity differs from BaseActivity in extended base class - to support compat
 *
 * Created by mattmehalso on 11/19/13.
 */
public class BaseActionBarActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((LaterApplication) getApplication()).inject(this);
    }
}
