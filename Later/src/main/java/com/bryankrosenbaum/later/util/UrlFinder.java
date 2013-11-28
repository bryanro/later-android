package com.bryankrosenbaum.later.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bryankrosenbaum.later.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Bryan on 11/2/13.
 */
public class UrlFinder {

    public static final String URL_REGEX_PATTERN = "((https?:\\/\\/)?[\\w-]{1,}(\\.[\\w-]{2,})+(:\\d+)?(\\/\\S*)?)[^.,)\\]\\*\\s]";
    public static String LINK_COLOR = "#006DB4";
    public static final String TWITTER_URL_PREFIX = "http://twitter.com/";

    /**
     * Format the item content to add highlighting for URIs by adding HTML
     * Ignores the http://twitter.com/ URIs that appear the bottom of every tweet shared (don't really care about seeing the tweet)
     *
     * @param originalContent The original string of item content (usually with one or more URIs)
     * @return Formatted HTML string that highlights the URIs
     */
    public static String formatContent(String originalContent) {
        String formattedContent = originalContent;
        Pattern urlPattern = Pattern.compile(URL_REGEX_PATTERN);
        Matcher urlMatcher = urlPattern.matcher(originalContent);
        Log.d("UrlFinder", "Entering urlMatcher while loop...");
        while(urlMatcher.find()) {
            Log.d("UrlFinder", "urlMatcher.group(): " + urlMatcher.group());
            if (!urlMatcher.group().contains(TWITTER_URL_PREFIX)) {
                formattedContent = formattedContent.replace(urlMatcher.group(), "<font color=\"" + LINK_COLOR + "\">" + urlMatcher.group() + "</font>");
            }
        }

        Log.d("UrlFinder", "formattedContent: " + formattedContent);

        formattedContent = formattedContent.replaceAll("\n", "<br />");

        return formattedContent;
    }

    /**
     * Finds all of the URIs in a string and return an array of them
     *
     * @param content The string content for the item that may or may not contain URIs
     * @return Array of strings, each containing the URIs found in the content parameter
     */
    public static String[] findUrlsInContent(String content) {
        ArrayList<String> urlList = new ArrayList<String>();
        Pattern urlPattern = Pattern.compile(URL_REGEX_PATTERN);
        Matcher urlMatcher = urlPattern.matcher(content);
        while(urlMatcher.find()) {
            if (!urlMatcher.group().contains(TWITTER_URL_PREFIX)) {
                urlList.add(urlMatcher.group());
            }
        }

        return urlList.toArray(new String[urlList.size()]);
    }

    public static void setLinkColor(String linkColor) {
        LINK_COLOR = linkColor;
    }
}
