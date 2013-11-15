package com.bryankrosenbaum.later.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bryankrosenbaum.later.R;
import com.bryankrosenbaum.later.data.LaterListDataSource;

public class ReceiveShareActivity extends Activity {

    /**
     * Receive the shared intent and process it if it's an ACTION_SEND intent, then close the activity when done processing.
     * This activity (as defined in the AndroidManifest is invisible because "@android:style/Theme.NoDisplay"
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiveshare);

        Resources res = getResources();
        String itemAddSuccessMessage = res.getString(R.string.item_added_successful);
        String itemAddFailMessage = res.getString(R.string.item_added_fail);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                if (handleSendText(intent)) {
                    Toast.makeText(getApplicationContext(), itemAddSuccessMessage, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), itemAddFailMessage, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            // Handle other intents, such as being started from the home screen
            Toast.makeText(getApplicationContext(), itemAddFailMessage, Toast.LENGTH_SHORT).show();
        }

        // close the activity
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.receive_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    /**
     * Take intent content and create a new item in the database, then show a toast message letting the user know it was successfully added.
     *
     * @param intent ACTION_SEND intent in plain/text form that contains EXTRA_TEXT with the content and an optional EXTRA_SUBJECT
     */
    private boolean handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        // if there is a title (such as the page title shared from a browser), append that to the front of the content text
        if (sharedTitle != null && sharedTitle.length() > 0) {
            sharedText = sharedTitle + " | " + sharedText;
        }

        if (sharedText != null) {
            try {
                LaterListDataSource dataSource = new LaterListDataSource(this);
                dataSource.open();
                dataSource.createItem(sharedText);
                dataSource.close();
                return true;
            }
            catch (SQLiteException sqlEx) {
                Log.e("handleSendText", "Exception attempting to open database connection: " + sqlEx.getMessage());
                return false;
            }
        }
        else {
            return false;
        }
    }
}
