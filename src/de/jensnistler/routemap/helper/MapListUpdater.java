package de.jensnistler.routemap.helper;

import java.io.IOException;
import java.util.Iterator;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class MapListUpdater extends AsyncTask<String, Integer, Integer> {
    private Context mContext;
    private MapAdapter mAdapter;
    private MapDataSource mDataSource;
    private ProgressDialog mDialog;

    public MapListUpdater(Context context, MapAdapter adapter, MapDataSource dataSource) {
        mContext = context;
        mAdapter = adapter;
        mDataSource = dataSource;
        mDialog = new ProgressDialog(mContext);
    }

    protected void onPreExecute() {
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();

        mDialog.setCancelable(false);
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
                        MapModel map = mDataSource.getMap(key);
                        if (null == map) {
                            map = new MapModel(key);
                            map.setUpdated(0);
                        }
                        map.setDescription(entry.getString("description"));
                        map.setDate(entry.getInt("date"));
                        map.setSize(entry.getLong("size"));
                        map.setUrl(entry.getString("url"));

                        if (true == mDataSource.saveMap(map)) {
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