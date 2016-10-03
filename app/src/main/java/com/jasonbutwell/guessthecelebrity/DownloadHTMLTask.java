package com.jasonbutwell.guessthecelebrity;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by J on 02/10/2016.
 */

public class DownloadHTMLTask extends AsyncTask<String, Void, String> {

    private URL url;
    private HttpURLConnection urlConnection = null;
    private int data = 0;
    private InputStream in;
    private InputStreamReader reader;
    private StringBuilder contentBuilder;

    @Override
    protected String doInBackground(String... params) {

        try {
            url = new URL(params[0]);
            urlConnection = (HttpURLConnection)url.openConnection();

            in = urlConnection.getInputStream();
            reader = new InputStreamReader(in);

            contentBuilder = new StringBuilder();

            while ( (data = in.read() ) != -1)
                contentBuilder.append((char)data);

            return contentBuilder.toString();

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return null;
    }
}
