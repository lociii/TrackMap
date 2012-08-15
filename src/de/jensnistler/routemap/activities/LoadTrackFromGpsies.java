package de.jensnistler.routemap.activities;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import de.jensnistler.routemap.R;
import de.jensnistler.routemap.helper.TrackDataSource;
import de.jensnistler.routemap.helper.TrackDownloader;
import de.jensnistler.routemap.helper.TrackListUpdater;
import de.jensnistler.routemap.helper.TrackModel;
import de.jensnistler.routemap.helper.TrackAdapter;

public class LoadTrackFromGpsies extends ListActivity {
    private TrackDataSource mDataSource;
    private TrackAdapter mAdapter;
    private String mPreferenceUser;
    private Long mPreferenceUpdate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_track);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceUser = prefs.getString("gpsiesUsername", null);
        mPreferenceUpdate = prefs.getLong("gpsiesLastUpdate", 0);

        mDataSource = new TrackDataSource(this);
        mDataSource.open();

        ArrayList<TrackModel> values = mDataSource.getAllTracks();
        mAdapter = new TrackAdapter(
            this,
            android.R.layout.simple_list_item_1,
            values
        );

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);

        // no gpsies user set
        if (mPreferenceUser == null || mPreferenceUser.trim().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.missingGpsiesUsername, Toast.LENGTH_LONG).show();
            Intent preferencesActivity = new Intent(getBaseContext(), Preferences.class);
            startActivity(preferencesActivity);
            return;
        }
        // load data for the first time
        else if (mPreferenceUser != null && mPreferenceUpdate == 0) {
            updateTrackList();
        }
    }

    private void updateTrackList() {
        mDataSource.deleteAll();

        new TrackListUpdater(this, this.mAdapter, this.mDataSource, TrackListUpdater.URL_TRACKS).execute(mPreferenceUser);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TrackModel selection = (TrackModel) l.getItemAtPosition(position);

        // check if track has already been cached
        File cacheDir = getExternalCacheDir();
        if (null == cacheDir || !cacheDir.canRead()) {
            Toast.makeText(getApplicationContext(), R.string.cannotReadFromCache, Toast.LENGTH_LONG).show();
            return;
        }

        File cacheFile = new File(cacheDir, selection.getKey() + ".gpx");
        if (cacheFile.exists()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("routeFile", cacheFile.getAbsolutePath());
            editor.commit();

            Intent mainActivity = new Intent(getBaseContext(), Main.class);
            startActivity(mainActivity);
            return;
        }

        // cache track
        new TrackDownloader(this).execute(selection);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loadroutefromgpsies, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                updateTrackList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
