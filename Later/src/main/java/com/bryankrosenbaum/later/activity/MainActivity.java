package com.bryankrosenbaum.later.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bryankrosenbaum.later.adapter.LaterListCursorAdapter;
import com.bryankrosenbaum.later.R;
import com.bryankrosenbaum.later.data.LaterListDataSource;
import com.bryankrosenbaum.later.data.LaterListItem;

import com.crashlytics.android.Crashlytics;

import java.util.Date;

import javax.inject.Inject;

import butterknife.InjectView;

public class MainActivity extends BaseActionBarActivity {

    private Context context = this;

    @Inject
    protected LaterListDataSource dataSource;

    @InjectView(android.R.id.list)
    protected ListView listView;

    private Cursor cursor;
    private LaterListCursorAdapter cursorAdapter;
    private boolean firstOnResume;
    private SupportMenuItem menuSearchFilter;
    private String selectedFilterText;
    private ActionMode mActionMode;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Override onCreate to initialize the activity, including: setting up the datasource and initializing the ListView
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(android.R.id.list);

        firstOnResume = true;

        initializeDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Later");
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        initializeList();
    }

    /**
     * Override onCreateOptionsMenu to setup each of the ActionBar items
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        initializeMenuSearch(menu);

        return true;
    }

    /**
     * Override onOptionsItemSelected to detect when a menu item is clicked
     *
     * @param item Menu item selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Override the onResume method so that the ListView gets refreshed each time the user enters the MainActivity
     */
    @Override
    public void onResume() {
        super.onResume();

        // only call refresh if this is not the first call to onResume (when the app starts up)
        if (firstOnResume) {
            firstOnResume = false;
        } else {
            refreshList();
        }
    }

    /**
     * Initialize the ListView by setting the OnItemClickListener to open up the website or a prompt
     */
    public void initializeList() {
        cursor = dataSource.fetchItems();

        cursorAdapter = new LaterListCursorAdapter(this, cursor);
        listView.setAdapter(cursorAdapter);

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
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final LaterListItem selectedItem = buildLaterListItemFromListItem(view);
                ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        // Inflate a menu resource providing context menu items
                        getMenuInflater().inflate(R.menu.context_menu, menu);
                        if (selectedItem.getStatus() == LaterListItem.STATUS_UNREAD) {
                            try {
                                menu.findItem(R.id.contextmenu_markread).setVisible(true);
                                menu.findItem(R.id.contextmenu_markunread).setVisible(false);
                            } catch (NullPointerException nullPointerEx) {
                                Log.e("onCreateActionMode", "Null Pointer: " + nullPointerEx.getMessage());
                            }
                        } else {
                            try {
                                menu.findItem(R.id.contextmenu_markread).setVisible(false);
                                menu.findItem(R.id.contextmenu_markunread).setVisible(true);
                            } catch (NullPointerException nullPointerEx) {
                                Log.e("onCreateActionMode", "Null Pointer: " + nullPointerEx.getMessage());
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.contextmenu_markread:
                                dataSource.markItemRead(selectedItem);
                                refreshList();
                                mActionMode.finish();
                                return true;
                            case R.id.contextmenu_markunread:
                                dataSource.markItemUnread(selectedItem);
                                refreshList();
                                mActionMode.finish();
                                return true;
                        }
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode actionMode) {
                        mActionMode = null;
                    }
                };
                mActionMode = startSupportActionMode(actionModeCallback);
                return true;
            }
        });

        // update display count of list items
        updateCountOfListItems(listView.getCount());
    }

    /**
     * Initialize the search menu item in the ActionBar
     *
     * @param menu
     */
    private void initializeMenuSearch(Menu menu) {
        menuSearchFilter = (SupportMenuItem) menu.findItem(R.id.menu_searchfilter);
        menuSearchFilter.setSupportOnActionExpandListener(new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                Log.d("onMenuItemActionExpand", "Expanded");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                Log.d("onMenuItemActionCollapse", "Collapsed");
                return true;
            }
        });

        SearchView searchView = (SearchView) menuSearchFilter.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d("onQueryTextSubmit", "String: " + s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d("onQueryTextChange", "String changed to: " + s);
                cursorAdapter.getFilter().filter(s);
                return true;
            }
        });

        cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return dataSource.fetchItems(constraint.toString());
            }
        });
    }

    /**
     *
     */
    private void initializeDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_listview);

        ArrayAdapter<CharSequence> drawerAdapter = ArrayAdapter.createFromResource(this, R.array.filter_array,
                android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter for the list view
        mDrawerList.setAdapter(drawerAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            String[] filterStringArray = getResources().getStringArray(R.array.filter_array);

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("onItemSelected", "You selected item: " + filterStringArray[position]);
                String filter = filterStringArray[position];

                // onItemSelected gets called when the view is being built when selectedFilterText == null
                // also refresh if the filter is changed, so compare against the previously selected filter
                if (selectedFilterText == null || !filter.equals(selectedFilterText)) {
                    selectedFilterText = filter;

                    if (filter.equals("All")) {
                        dataSource.setFilter(LaterListDataSource.Filter.ALL);
                        refreshList();
                        mDrawerLayout.closeDrawer(mDrawerList);
                    } else if (filter.equals("Unread")) {
                        dataSource.setFilter(LaterListDataSource.Filter.UNREAD);
                        refreshList();
                        mDrawerLayout.closeDrawer(mDrawerList);
                    } else if (filter.equals("Read")) {
                        dataSource.setFilter(LaterListDataSource.Filter.READ);
                        refreshList();
                        mDrawerLayout.closeDrawer(mDrawerList);
                    }
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * Refresh the ListView
     */
    public void refreshList() {
        cursor = dataSource.fetchItems();

        cursorAdapter.changeCursor(cursor);

        updateCountOfListItems(cursor.getCount());

        menuSearchFilter.collapseActionView();
    }

    /**
     * Update the display showing the count of items in the ListView, currently displayed in the ActionBar's subtitle.
     * If the count exceeds the MAX_COUNT_TO_SHOW, then it will show [MAX_COUNT_TO_SHOW]+.  (e.g.: "100+")
     * The current format is: [count|MAX_COUNT_TO_SHOW+] ["read"|"unread"|""] item[s].
     *
     * @param count Count of items that should be displayed
     */
    public void updateCountOfListItems(int count) {
        int MAX_COUNT_TO_SHOW = 100;
        StringBuilder subtitleStringBuilder = new StringBuilder();

        if (count <= MAX_COUNT_TO_SHOW) {
            subtitleStringBuilder.append(count);
        } else {
            subtitleStringBuilder.append(MAX_COUNT_TO_SHOW).append("+");
        }

        switch(dataSource.getFilter()) {
            case READ:
                subtitleStringBuilder.append(" read");
                break;
            case UNREAD:
                subtitleStringBuilder.append(" unread");
                break;
            case ALL:
                break;
        }

        subtitleStringBuilder.append(" item");
        if (count != 1) {
            subtitleStringBuilder.append("s");
        }

        getSupportActionBar().setSubtitle(subtitleStringBuilder.toString());
    }

    /**
     * Open the URI by creating a browser intent and mark the ListView item as read
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
    private void markItemAsRead(LaterListItem item) {
        dataSource.markItemRead(item);
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
        } catch (Exception ex) {
            Log.e("MainActivity", "Exception parsing int (" + statusTextview.getText().toString() + "): " + ex.getMessage());
        }

        LaterListItem item = new LaterListItem(id, content, addDtm, status);
        return item;
    }
}
