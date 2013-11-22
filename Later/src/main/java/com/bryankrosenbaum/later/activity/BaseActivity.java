package com.bryankrosenbaum.later.activity;

import android.app.Activity;
import android.os.Bundle;

import com.bryankrosenbaum.later.LaterApplication;

/**
 * See notes for BaseActionBarActivity.
 *
 * Created by mattmehalso on 11/19/13.
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((LaterApplication) getApplication()).inject(this);
    }
}
