package com.bryankrosenbaum.later.data;

import com.bryankrosenbaum.later.util.UrlFinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Bryan on 10/29/13.
 */
public class LaterListItem {
    private long id;
    private String content;
    private Date addDtm;
    private int status;
    private String[] urls;

    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    public static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public LaterListItem(long id, String content, Date addDtm, int status) {
        init(id, content, addDtm, status);
    }

    public LaterListItem(long id, String content, long addDtm, int status) {
        Date dateAddDtm = getDateFromFormattedLong(addDtm);
        init(id, content, dateAddDtm, status);
    }

    /**
     * Helper function for the multiple constructors that creates a new LaterListItem object
     *
     * @param id ID
     * @param content String content of
     * @param addDtm Date/time that item was created
     * @param status Status of item (STATUS_UNREAD=0 or STATUS_READ=1)
     */
    private void init(long id, String content, Date addDtm, int status) {
        this.id = id;
        this.content = content;
        this.addDtm = addDtm;
        this.status = status;

        this.urls = UrlFinder.findUrlsInContent(this.content);
    }

    /**
     * Format date object as a long to allow it to be saved to the database.
     * SQLite database does not permit Date fields so instead store as a long.
     *
     * @param date Date object to be formatted
     * @return Long representation of the date that can be saved in the database
     */
    public static long formatDateAsLong(Date date){
        return Long.parseLong(dateFormat.format(date.getTime()));
    }

    /**
     * Convert long representation of a date to a Date object.
     * SQLite database does not permit Date fields so instead store as a long.
     *
     * @param longDate Long representation of the date
     * @return Date object
     */
    public static Date getDateFromFormattedLong(long longDate){
        try {
            Date d = dateFormat.parse(String.valueOf(longDate));
            return d;

        } catch (ParseException e) {
            return null;
        }
    }

    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public Date getAddDtm() {
        return addDtm;
    }
    public void setAddDtm(long addDtm) {
        this.addDtm = getDateFromFormattedLong(addDtm);
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String[] getUrls() {
        return urls;
    }
    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    @Override
    public String toString() {
        return this.content;
    }
}
