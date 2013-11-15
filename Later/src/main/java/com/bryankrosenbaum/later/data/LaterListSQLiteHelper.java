package com.bryankrosenbaum.later.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Bryan on 10/29/13.
 */
public class LaterListSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_LATER_LIST = "later_list";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_ADD_DTM = "add_dtm";
    public static final String COLUMN_STATUS = "status";

    private static final String DATABASE_NAME = "later_list.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_LATER_LIST + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_CONTENT + " text not null, "
            + COLUMN_ADD_DTM + " integer not null, "
            + COLUMN_STATUS + " integer not null"
            + ");";

    public LaterListSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d("LaterListSQLiteHelper", "Creating database");
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LaterListSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LATER_LIST);
        onCreate(db);
    }
}
