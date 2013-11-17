package com.bryankrosenbaum.later.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bryankrosenbaum.later.R;
import com.bryankrosenbaum.later.data.LaterListItem;
import com.bryankrosenbaum.later.data.LaterListSQLiteHelper;
import com.bryankrosenbaum.later.util.UrlFinder;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Bryan on 10/31/13.
 */
public class LaterListCursorAdapter extends CursorAdapter {

    public static final String DATE_FORMAT_PRESENTABLE = "K:mm a | MMM d";
    private static final SimpleDateFormat presentableDateFormat = new SimpleDateFormat(DATE_FORMAT_PRESENTABLE);

    public LaterListCursorAdapter(Activity context, Cursor c) {
        super (context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        // tell adapters how each item will look when view is created for the first time
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View retView = inflater.inflate(R.layout.layout_listitem, viewGroup, false);
        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // take data from cursor and put it in the view

        TextView textviewContent = (TextView) view.findViewById(R.id.listitem_textview_content);
        String content = cursor.getString(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_CONTENT));
        String formattedContent = UrlFinder.formatContent(content);
        textviewContent.setText(Html.fromHtml(formattedContent));

        TextView textviewTime = (TextView) view.findViewById(R.id.listitem_textview_time);
        Date dateAddDtm = LaterListItem.getDateFromFormattedLong(cursor.getLong(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ADD_DTM)));
        textviewTime.setText(presentableDateFormat.format(dateAddDtm));

        TextView textviewId = (TextView) view.findViewById(R.id.listitem_textview_id);
        String id = cursor.getString(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ID));
        textviewId.setText(id);

        TextView textviewStatus = (TextView) view.findViewById(R.id.listitem_textview_status);
        int status = cursor.getInt(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_STATUS));
        textviewStatus.setText(Integer.toString(status));
    }
}
