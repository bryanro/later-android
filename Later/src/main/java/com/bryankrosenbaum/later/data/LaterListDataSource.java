package com.bryankrosenbaum.later.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Bryan on 10/29/13.
 */
public class LaterListDataSource {

    // Filter
    public enum Filter { ALL, UNREAD, READ };
    private Filter filter;
    public Filter getFilter() {
        return filter;
    }
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    // Database and db fields
    private SQLiteDatabase database;
    private LaterListSQLiteHelper dbHelper;
    private String[] allColumns = {
            LaterListSQLiteHelper.COLUMN_ID,
            LaterListSQLiteHelper.COLUMN_CONTENT,
            LaterListSQLiteHelper.COLUMN_ADD_DTM,
            LaterListSQLiteHelper.COLUMN_STATUS
    };

    /**
     * Constructor that initializes the dbHelper and sets the filter default to UNREAD
     *
     * @param context
     */
    public LaterListDataSource(Context context) {
        Log.d("LaterListDataSource", "initialize dbHelper in constructor");
        dbHelper = new LaterListSQLiteHelper(context);

        // initialize filter to UNREAD
        setFilter(Filter.UNREAD);
    }

    /**
     * Open the database connection
     *
     * @throws SQLException Exception opening a writeable database
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Close the database connection
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Create a new item in the database based on the content string parameter.
     * ADD_DTM will be defaulted to the current time.
     * STATUS will be defaulted to UNREAD.
     *
     * @param content String containing the text of the item that will be created
     * @return LaterListItem object created
     */
    public LaterListItem createItem(String content) {
        // open db connection
        this.open();

        ContentValues values = new ContentValues();

        values.put(LaterListSQLiteHelper.COLUMN_CONTENT, content);
        values.put(LaterListSQLiteHelper.COLUMN_ADD_DTM, LaterListItem.formatDateAsLong(new Date()));
        values.put(LaterListSQLiteHelper.COLUMN_STATUS, LaterListItem.STATUS_UNREAD);

        long insertId = database.insert(LaterListSQLiteHelper.TABLE_LATER_LIST, "NullColHackThrowError", values);
        Cursor cursor = database.query(LaterListSQLiteHelper.TABLE_LATER_LIST,
                allColumns, LaterListSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        LaterListItem newListItem = cursorToItem(cursor);
        cursor.close();

        // close db connection
        this.close();

        return newListItem;
    }

    /**
     * Delete item from database
     *
     * @param item LaterListItem that will be deleted
     */
    public void deleteItem(LaterListItem item) {
        // open db connection
        this.open();

        long id = item.getId();
        Log.d("LaterListDataSource", "LaterList item deleted with id: " + id);
        database.delete(LaterListSQLiteHelper.TABLE_LATER_LIST, LaterListSQLiteHelper.COLUMN_ID
                + " = " + id, null);

        // close db connection
        this.close();
    }

    /**
     * Mark an item as read
     * @param item LaterListItem that will be marked as read
     */
    public void markItemRead(LaterListItem item) {
        // open db connection
        this.open();

        long id = item.getId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LaterListSQLiteHelper.COLUMN_STATUS, LaterListItem.STATUS_READ);
        database.update(LaterListSQLiteHelper.TABLE_LATER_LIST, contentValues, LaterListSQLiteHelper.COLUMN_ID + "=" + id, null);

        // close db connection
        this.close();
    }

    /**
     * Query the database to get items based on what the filter is set to (ALL, READ, UNREAD)
     *
     * @return Cursor containing items
     */
    public Cursor fetchItems() {
        return fetchItems(null);
    }

    public Cursor fetchItems(String searchText) {

        // open db connection
        this.open();
        /*
        switch (getFilter()) {
            case UNREAD:
                return fetchUnreadItems();
            case ALL:
                return fetchAllItems();
            case READ:
                return fetchReadItems();
            default:
                return fetchUnreadItems();
        }*/

        List<String> whereClause = new ArrayList<String>();
        if (getFilter() == Filter.UNREAD) {
            whereClause.add(LaterListSQLiteHelper.COLUMN_STATUS + "=" + LaterListItem.STATUS_UNREAD);
        }
        else if (getFilter() == Filter.READ) {
            whereClause.add(LaterListSQLiteHelper.COLUMN_STATUS + "=" + LaterListItem.STATUS_READ);
        }

        if (searchText != null && searchText.length() > 0) {
            whereClause.add(LaterListSQLiteHelper.COLUMN_CONTENT + " LIKE '%" + searchText + "%'");
        }

        String where = android.text.TextUtils.join(" AND ", whereClause.toArray());

        Log.d("fetchItems", "where clause: " + where);

        Cursor cursor = database.query(LaterListSQLiteHelper.TABLE_LATER_LIST,
                allColumns, where,
                null, null, null,  LaterListSQLiteHelper.COLUMN_ADD_DTM + " asc");

        if (cursor != null) {
            cursor.moveToFirst();
        }

        // close db connection
        this.close();

        return cursor;
    }

    /**
     * Query the database to get all of the items (both read and unread)
     *
     * @return Cursor containing all items
     */
    public Cursor fetchAllItems() {
        Cursor cursor = database.query(LaterListSQLiteHelper.TABLE_LATER_LIST,
                allColumns, null, null, null, null, LaterListSQLiteHelper.COLUMN_ADD_DTM + " asc");

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    /**
     * Query the database to get all of the unread items
     *
     * @return Cursor containing unread items
     */
    public Cursor fetchUnreadItems() {
        Cursor cursor = database.query(LaterListSQLiteHelper.TABLE_LATER_LIST,
                allColumns, LaterListSQLiteHelper.COLUMN_STATUS + "=" + LaterListItem.STATUS_UNREAD,
                null, null, null,  LaterListSQLiteHelper.COLUMN_ADD_DTM + " asc");

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    /**
     * Query the database to get all of the read items
     *
     * @return Cursor containing read items
     */
    public Cursor fetchReadItems() {
        Cursor cursor = database.query(LaterListSQLiteHelper.TABLE_LATER_LIST,
                allColumns, LaterListSQLiteHelper.COLUMN_STATUS + "=" + LaterListItem.STATUS_READ,
                null, null, null,  LaterListSQLiteHelper.COLUMN_ADD_DTM + " asc");

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    /**
     * Delete all items from the database
     */
    public void deleteAll() {
        // open db connection
        this.open();

        database.delete(LaterListSQLiteHelper.TABLE_LATER_LIST, null, null);

        // close db connection
        this.close();
    }

    /**
     * Create a LaterListItem from the cursor parameter
     *
     * @param cursor Cursor that contains the item content
     * @return LaterListItem object created from the cursor
     */
    private LaterListItem cursorToItem(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ID));
        String content = cursor.getString(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_CONTENT));
        long addDtm = cursor.getLong(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ADD_DTM));
        int status = cursor.getInt(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_STATUS));
        LaterListItem laterListItem = new LaterListItem(id, content, addDtm, status);
        return laterListItem;
    }
}
