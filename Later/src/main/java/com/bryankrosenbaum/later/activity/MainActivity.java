package com.bryankrosenbaum.later.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bryankrosenbaum.later.adapter.LaterListCursorAdapter;
import com.bryankrosenbaum.later.R;
import com.bryankrosenbaum.later.data.LaterListDataSource;
import com.bryankrosenbaum.later.data.LaterListItem;

import com.crashlytics.android.Crashlytics;
import java.util.Date;

public class MainActivity extends ListActivity {

    private Context context = this;
    private LaterListDataSource dataSource;
    private ListView listView;
    private Cursor cursor;
    private LaterListCursorAdapter cursorAdapter;
    private boolean firstOnResume;

    private MenuItem menuShowAll;
    private MenuItem menuShowUnread;
    private MenuItem menuSpinnerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_main);

        dataSource = new LaterListDataSource(this);
        listView = (ListView) findViewById(android.R.id.list);

        firstOnResume = true;

        initializeList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menuShowAll = menu.findItem(R.id.menu_showall);
        menuShowUnread = menu.findItem(R.id.menu_showunread);

        menuSpinnerItem = menu.findItem(R.id.menu_spinner_view);
        Spinner spinner = (Spinner) menuSpinnerItem.getActionView().findViewById(R.id.spinner_filter);

        //Spinner spinner = (Spinner) findViewById(R.id.spinner_filter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String[] filterStringArray = getResources().getStringArray(R.array.filter_array);

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                Log.d("onItemSelected", "You selected item: " + filterStringArray[pos]);
                String filter = filterStringArray[pos];
                if (filter.equals("All")) {
                    dataSource.setFilter(LaterListDataSource.Filter.ALL);
                    refreshList();
                }
                else if (filter.equals("Unread")) {
                    dataSource.setFilter(LaterListDataSource.Filter.UNREAD);
                    refreshList();
                }
                else if (filter.equals("Read")) {
                    dataSource.setFilter(LaterListDataSource.Filter.READ);
                    refreshList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.menu_refresh: {
                refreshList();
                break;
            }
            case R.id.menu_showall: {
                dataSource.setFilter(LaterListDataSource.Filter.ALL);
                menuShowAll.setVisible(false);
                menuShowUnread.setVisible(true);
                refreshList();
                break;
            }
            case R.id.menu_showunread: {
                dataSource.setFilter(LaterListDataSource.Filter.UNREAD);
                menuShowAll.setVisible(true);
                menuShowUnread.setVisible(false);
                refreshList();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // only call refresh if this is not the first call to onResume (when the app starts up)
        if (firstOnResume) {
            firstOnResume = false;
        }
        else {
            refreshList();
        }
    }

    public void initializeList() {
        dataSource.open();
        cursor = dataSource.fetchItems();
        dataSource.close();

        cursorAdapter = new LaterListCursorAdapter(this, cursor);
        setListAdapter(cursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final LaterListItem selectedItem = buildLaterListItemFromListItem(view);
                final String[] urls = selectedItem.getUrls();

                if (urls.length < 1) {
                    Toast.makeText(getApplicationContext(), "No URLs found.", Toast.LENGTH_SHORT).show();
                    markItemAsRead(selectedItem);
                } else if (urls.length == 1) {
                    Log.d("MainActivity", "Uri.parse(urls[0]): " + urls[0]);
                    openUri(urls[0], selectedItem);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("");
                    builder.setItems(urls, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            Log.d("MainActivity", "Url clicked from dialog: " + urls[which]);
                            openUri(urls[which], selectedItem);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int pos, long id) {

                final LaterListItem selectedItem = buildLaterListItemFromListItem(view);

                return true;
            }
        });
    }

    public void refreshList() {
        dataSource.open();
        cursor = dataSource.fetchItems();
        dataSource.close();

        cursorAdapter.changeCursor(cursor);
    }

    /**
     * Open the URI by creating a browser intent and mark the listview item as read
     *
     * @param url URI string that will be opened in the browser
     * @param item Listview item that contains the URI that will be marked as read
     */
    private void openUri(String url, LaterListItem item) {
        // open URI and mark item as read
        Uri uri = Uri.parse(url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(browserIntent);

        markItemAsRead(item);
    }

    /**
     * Mark the item as read
     *
     * @param item Listview item that will be marked as read
     */
    private void markItemAsRead (LaterListItem item) {
        dataSource.open();
        dataSource.markItemRead(item);
        dataSource.close();
    }

    /**
     * Create a LaterListItem from the listview
     *
     * @param view Listview that contains the information that the LaterListItem will be built from
     * @return LaterListItem that represents the listview item
     */
    private LaterListItem buildLaterListItemFromListItem(View view) {
        TextView idTextview = (TextView) view.findViewById(R.id.listitem_textview_id);
        long id = Long.parseLong(idTextview.getText().toString());

        TextView contentTextview = (TextView) view.findViewById(R.id.listitem_textview_content);
        String content = contentTextview.getText().toString();

        // TODO: SET ADD_DTM (not used now so lower priority)
        TextView addDtmTextview = (TextView) view.findViewById(R.id.listitem_textview_time);

        Date addDtm = new Date();

        TextView statusTextview = (TextView) view.findViewById(R.id.listitem_textview_status);
        int status = 0;
        try {
            status = Integer.parseInt(statusTextview.getText().toString());
        }
        catch (Exception ex) {
            Log.e("MainActivity", "Exception parsing int (" + statusTextview.getText().toString() + "): " + ex.getMessage());
        }

        LaterListItem item = new LaterListItem(id, content, addDtm, status);
        return item;
    }
}
