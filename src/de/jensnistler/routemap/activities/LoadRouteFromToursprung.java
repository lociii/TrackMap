package de.jensnistler.routemap.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import de.jensnistler.routemap.R;
import de.jensnistler.routemap.helper.TrackModel;

public class LoadRouteFromToursprung extends ListActivity {
    private static final int BIKEMAP = 1;
    private static final String BIKEMAP_URL_TRACK = "http://www.bikemap.net/user/<user>/search.json?page=<page>&collected=true";
    private static final String BIKEMAP_URL_GPX = "http://www.bikemap.net/route/<id>/export.gpx";
    
    private static final int INLINEMAP = 2;
    private static final String INLINEMAP_URL_TRACK = "http://www.inlinemap.net/user/<user>/search.json?page=<page>&collected=true";
    private static final String INLINEMAP_URL_GPX = "http://www.inlinemap.net/route/<id>/export.gpx";
    
    private static final int MOPEDMAP = 3;
    private static final String MOPEDMAP_URL_TRACK = "http://www.mopedmap.net/user/<user>/search.json?page=<page>&collected=true";
    private static final String MOPEDMAP_URL_GPX = "http://www.mopedmap.net/route/<id>/export.gpx";
    
    private static final int RUNMAP = 4;
    private static final String RUNMAP_URL_TRACK = "http://www.runmap.net/user/<user>/search.json?page=<page>&collected=true";
    private static final String RUNMAP_URL_GPX = "http://www.runmap.net/route/<id>/export.gpx";
    
    private static final int WANDERMAP = 5;
    private static final String WANDERMAP_URL_TRACK = "http://www.wandermap.net/user/<user>/search.json?page=<page>&collected=true";
    private static final String WANDERMAP_URL_GPX = "http://www.wandermap.net/route/<id>/export.gpx";

    private TrackAdapter mAdapter;
    
    private Integer mPreferenceType;
    private String mPreferenceName;

    private class TrackAdapter extends ArrayAdapter<TrackModel> {
        private ArrayList<TrackModel> items;

        public TrackAdapter(Context context, int textViewResourceId, ArrayList<TrackModel> items) {
            super(context, textViewResourceId, items);
            this.items = items;
            sortData();
        }

        public void add(TrackModel map) {
            super.add(map);
            sortData();
        }

        private void sortData() {
            sort(new Comparator<TrackModel>() {
                public int compare(TrackModel object1, TrackModel object2) {
                    return object1.toString().compareTo(object2.toString());
                };
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.load_tracks_row, null);
            }
            TrackModel track = items.get(position);
            if (track != null) {
                TextView tt = (TextView) v.findViewById(R.id.text);
                if (tt != null) {
                    tt.setText(track.getDescription());
                }
            }
            return v;
        }
    }

    private class UpdateTrackList extends AsyncTask<String, Integer, Integer> {
        private final ProgressDialog mDialog = new ProgressDialog(LoadRouteFromToursprung.this);

        protected void onPreExecute() {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();

            mDialog.setCancelable(false);
            mDialog.setMessage("Loading track list...");
            mDialog.show();
        }

        protected Integer doInBackground(String... urls) {
            Pattern p = Pattern.compile("<a href=\"/route/(\\d+)\">(.+)</a>");
            int trackCount = 0;

            for (String url: urls) {
                int page = 1;

                // loop until all tracks have been found
                while (true) {
                    String urlPage = url.replace("<page>", page + "");

                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(urlPage); 
    
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    String jsonString = "";
                    try {
                        jsonString = httpclient.execute(httpget, responseHandler);
                    }
                    catch (IOException e) {
                        break;
                    }
    
                    JSONObject jsonObject = new JSONObject();
                    String html = "";
                    try {
                        jsonObject = new JSONObject(jsonString);
                        html = jsonObject.getString("panel");
                    }
                    catch (JSONException e) {
                        break;
                    }

                    Matcher m = p.matcher(html);
                    int pageTrackCount = 0;
                    while (m.find()) {
                        TrackModel track = new TrackModel(Integer.parseInt(m.group(1)));
                        track.setDescription(Html.fromHtml((String) m.group(2)).toString());
                        mAdapter.add(track);
                        pageTrackCount++;
                        trackCount++;
                    }

                    // last page
                    if (pageTrackCount < 10) {
                        break;
                    }

                    page++;
                }
            }

            return trackCount;
        }

        protected void onPostExecute(Integer count) {
            mDialog.dismiss();
            mAdapter.setNotifyOnChange(true);

            if (count > 0) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class DownloadTrack extends AsyncTask<TrackModel, Integer, Integer> {
        private final ProgressDialog mDialog = new ProgressDialog(LoadRouteFromToursprung.this);
        private boolean mRunning = true;

        protected void onPreExecute() {
            mDialog.setMessage("Downloading track...");
            mDialog.setCancelable(true);
            mDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    onCancelled();
                }
            });
            mDialog.show();
        }

        protected void onCancelled() {
            mRunning = false;
        }

        protected Integer doInBackground(TrackModel... tracks) {
            Integer count = 0;

            for (TrackModel track: tracks) {
                try {
                    String trackUrl = "";
                    switch (mPreferenceType) {
                        case BIKEMAP:
                            trackUrl = BIKEMAP_URL_GPX.replace("<id>", track.getKey() + "");
                            break;
                        case RUNMAP:
                            trackUrl = RUNMAP_URL_GPX.replace("<id>", track.getKey() + "");
                            break;
                        case WANDERMAP:
                            trackUrl = WANDERMAP_URL_GPX.replace("<id>", track.getKey() + "");
                            break;
                        case INLINEMAP:
                            trackUrl = INLINEMAP_URL_GPX.replace("<id>", track.getKey() + "");
                            break;
                        case MOPEDMAP:
                            trackUrl = MOPEDMAP_URL_GPX.replace("<id>", track.getKey() + "");
                            break;
                    }

                    if (0 == trackUrl.length()) {
                        continue;
                    }

                    URL url = new URL(trackUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    // disguise as chrome since toursprung even redirects .gpx file requests to mobile web
                    urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6");
                    urlConnection.connect();

                    File cacheDir = getExternalCacheDir();
                    if (null == cacheDir || !cacheDir.canWrite()) {
                        continue;
                    }
                    File cacheFile = new File(cacheDir, "toursprung.gpx");

                    FileOutputStream fileOutput = new FileOutputStream(cacheFile);
                    InputStream inputStream = urlConnection.getInputStream();

                    int totalSize = urlConnection.getContentLength();
                    int downloadedSize = 0;

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;

                    while (true == mRunning && (bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;

                        publishProgress(totalSize, downloadedSize);
                    }

                    //close the output stream when done
                    urlConnection.disconnect();
                    fileOutput.close();

                    // file was not downloaded completely
                    if (downloadedSize < totalSize) {
                        cacheFile.delete();
                        continue;
                    }
                    

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("routeFile", cacheFile.getAbsolutePath());
                    editor.commit();

                    count++;
                //catch some possible errors...
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return count; 
        }

        protected void onProgressUpdate(Integer... progress) {
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setMax(progress[0]);
            mDialog.setProgress(progress[1]);
        }

        protected void onPostExecute(Integer count) {
            mDialog.dismiss();

            if (count > 0) {
                Intent mainActivity = new Intent(getBaseContext(), Main.class);
                startActivity(mainActivity);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_tracks);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceType = prefs.getInt("toursprungType", 0);
        mPreferenceName = prefs.getString("toursprungName", null);

        mAdapter = new TrackAdapter (
            this,
            android.R.layout.simple_list_item_1,
            new ArrayList<TrackModel>()
        );

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);

        if (mPreferenceType != 0 && mPreferenceName != null) {
            // load track list
            String url = "";
            switch (mPreferenceType) {
                case BIKEMAP:
                    url = BIKEMAP_URL_TRACK.replace("<user>", mPreferenceName);
                    break;
                case RUNMAP:
                    url = RUNMAP_URL_TRACK.replace("<user>", mPreferenceName);
                    break;
                case WANDERMAP:
                    url = WANDERMAP_URL_TRACK.replace("<user>", mPreferenceName);
                    break;
                case INLINEMAP:
                    url = INLINEMAP_URL_TRACK.replace("<user>", mPreferenceName);
                    break;
                case MOPEDMAP:
                    url = MOPEDMAP_URL_TRACK.replace("<user>", mPreferenceName);
                    break;
            }
            new UpdateTrackList().execute(url);
        }
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        TrackModel selection = (TrackModel) l.getItemAtPosition(position);
        new DownloadTrack().execute(selection);
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.toursprung_settings);
        dialog.setTitle(R.string.toursprung_settings);

        if (0 != mPreferenceType) {
            Spinner typeInput = (Spinner) dialog.findViewById(R.id.type);
            typeInput.setSelection(mPreferenceType - 1);
        }

        if (null != mPreferenceName) {
            EditText usernameInput = (EditText) dialog.findViewById(R.id.username);
            usernameInput.setText(mPreferenceName);
        }

        Button saveButton = (Button) dialog.findViewById(R.id.save);
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                View parent = (View)v.getParent();

                EditText usernameInput = (EditText) parent.findViewById(R.id.username);
                mPreferenceName = usernameInput.getText().toString();

                Spinner typeInput = (Spinner) parent.findViewById(R.id.type);
                mPreferenceType = typeInput.getSelectedItemPosition() + 1;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("toursprungType", mPreferenceType);
                editor.putString("toursprungName", mPreferenceName);
                editor.commit();

                removeDialog(1);
            }
        });

        return dialog;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loadroutefromtoursrpung, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                showDialog(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
