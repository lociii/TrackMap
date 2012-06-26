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
import java.util.Iterator;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.jensnistler.routemap.R;
import de.jensnistler.routemap.helper.MapDataSource;
import de.jensnistler.routemap.helper.MapModel;

public class ManageMaps extends ListActivity {
    private MapDataSource mDatasource;
    private MapAdapter mAdapter;

    private class MapAdapter extends ArrayAdapter<MapModel> {
        private ArrayList<MapModel> items;
        private Context context;

        public MapAdapter(Context context, int textViewResourceId, ArrayList<MapModel> items) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.context = context;
            sortData();
        }

        public void add(MapModel map) {
            super.add(map);
            sortData();
        }

        private void sortData() {
            sort(new Comparator<MapModel>() {
                public int compare(MapModel object1, MapModel object2) {
                    return object1.toString().compareTo(object2.toString());
                };
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.load_map_row, null);
            }
            MapModel map = items.get(position);
            if (map != null) {
                ImageView ic = (ImageView) v.findViewById(R.id.icon);
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null) {
                    tt.setText(map.getDescription());
                }
                if (bt != null){
                    bt.setText("Size: " + map.getSize() + " MB");
                }
                if (ic != null) {
                    Log.i("Map", map.getKey() + " - " + map.getDate() + " / " + map.getUpdated());

                    if (0 == map.getUpdated()) {
                        ic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.map_add));
                    }
                    else {
                        Log.i("Date", map.getDescription());
                        Log.i("Updated", map.getUpdated() + "");
                        Log.i("Date", map.getDate() + "");
                        
                        if (map.getUpdated() >= map.getDate()) {
                            ic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.map_ok));
                        }
                        else {
                            ic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.map_refresh));
                        }
                    }
                }
            }
            return v;
        }
    }

    private class UpdateMapList extends AsyncTask<String, Integer, Integer> {
        private final ProgressDialog mDialog = new ProgressDialog(ManageMaps.this);

        protected void onPreExecute() {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();

            mDialog.setMessage("Loading map list...");
            mDialog.show();
        }

        @SuppressWarnings("unchecked")
        protected Integer doInBackground(String... urls) {
            Integer count = 0;

            for (String url: urls) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url); 

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String jsonString = "";
                try {
                    jsonString = httpclient.execute(httpget, responseHandler);
                }
                catch (IOException e) {
                    // use empty jsonString
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = new JSONObject(jsonString);
                }
                catch (JSONException e) {
                    // use empty jsonObject
                }

                if (0 < jsonObject.length()) {
                    try {
                        Iterator<String> iterator = jsonObject.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            JSONObject entry = jsonObject.getJSONObject(key);

                            // add to list and save in database
                            MapModel map = mDatasource.getMap(key);
                            if (null == map) {
                                map = new MapModel(key);
                                map.setUpdated(0);
                            }
                            map.setDescription(entry.getString("description"));
                            map.setDate(entry.getInt("date"));
                            map.setSize(entry.getLong("size"));
                            map.setUrl(entry.getString("url"));

                            if (true == mDatasource.saveMap(map)) {
                                mAdapter.add(map);
                            }

                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return count; 
        }

        protected void onPostExecute(Integer count) {
            mDialog.dismiss();
            mAdapter.setNotifyOnChange(true);

            if (count > 0) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class DownloadMap extends AsyncTask<MapModel, Integer, Integer> {
        private final ProgressDialog mDialog = new ProgressDialog(ManageMaps.this);

        protected void onPreExecute() {
            mAdapter.setNotifyOnChange(false);

            mDialog.setMessage("Downloading map...");
            mDialog.setCancelable(false);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setProgress(0);
            mDialog.show();
        }

        protected Integer doInBackground(MapModel... maps) {
            Integer count = 0;

            for (MapModel map: maps) {
                try {
                    URL url = new URL(map.getUrl());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    File cacheDir = getExternalCacheDir();
                    if (null == cacheDir || !cacheDir.canWrite()) {
                        continue;
                    }
                    File cacheFile = new File(cacheDir, map.getKey().replace("/", "_") + ".map");

                    FileOutputStream fileOutput = new FileOutputStream(cacheFile);
                    InputStream inputStream = urlConnection.getInputStream();

                    int totalSize = urlConnection.getContentLength();
                    int downloadedSize = 0;

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;

                        publishProgress(totalSize, downloadedSize);
                    }

                    //close the output stream when done
                    fileOutput.close();

                    // update map dataset
                    mAdapter.remove(map);
                    map.setUpdated(map.getDate());
                    mDatasource.saveMap(map);
                    mAdapter.add(map);
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
            mDialog.setMax(progress[0]);
            mDialog.setProgress(progress[1]);
        }

        protected void onPostExecute(Integer count) {
            mDialog.dismiss();

            mAdapter.setNotifyOnChange(true);
            if (count > 0) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatasource = new MapDataSource(this);
        mDatasource.open();

        ArrayList<MapModel> values = mDatasource.getAllMaps();
        mAdapter = new MapAdapter (
            this,
            android.R.layout.simple_list_item_1,
            values
        );

        ListView list = getListView();
        list.setAdapter(mAdapter);

        if (values.isEmpty()) {
            new UpdateMapList().execute("http://static.jensnistler.de/maps.json");
        }

        registerForContextMenu(list);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        MapModel selection = (MapModel) l.getItemAtPosition(position);
        if (selection.getUpdated() != 0) {
            Toast.makeText(this, "Loading: " + selection.getDescription(), Toast.LENGTH_LONG).show();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("mapFile", selection.getKey());
            editor.commit();

            Intent mainActivity = new Intent(getBaseContext(), Main.class);
            startActivity(mainActivity);
        }
        else {
            v.showContextMenu();
        }
    }

    private MapModel contextSelection;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView view = (ListView) v;
        contextSelection = (MapModel) view.getItemAtPosition(info.position);

        menu.setHeaderTitle(contextSelection.getDescription());
        if (contextSelection.getUpdated() != 0) {
            if (contextSelection.getDate() < contextSelection.getUpdated()) {
                menu.add(Menu.NONE, 1, 1, "Update");
            }
            menu.add(Menu.NONE, 2, 2, "Remove");
        }
        else {
            menu.add(Menu.NONE, 3, 3, "Download");
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // remove
            case 2:
                File cacheDir = getExternalCacheDir();
                if (null == cacheDir || !cacheDir.canWrite()) {
                    Toast.makeText(this, "Cache is not writable", Toast.LENGTH_LONG).show();
                    return true;
                }
                File cacheFile = new File(cacheDir, contextSelection.getKey().replace("/", "_") + ".map");
                cacheFile.delete();

                mAdapter.remove(contextSelection);
                contextSelection.setUpdated(0);
                mDatasource.saveMap(contextSelection);
                mAdapter.add(contextSelection);
                mAdapter.setNotifyOnChange(true);
                mAdapter.notifyDataSetChanged();
                break;

            // download
            case 1:
            case 3:
                new DownloadMap().execute(contextSelection);
                break;
        }

        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.managemaps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_updatemaps:
                new UpdateMapList().execute("http://static.jensnistler.de/maps.json");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
