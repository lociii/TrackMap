package de.jensnistler.routemap.activities;

import de.jensnistler.routemap.R;
import de.jensnistler.routemap.fragments.TrackListAbstract;
import de.jensnistler.routemap.fragments.TrackListOwn;
import de.jensnistler.routemap.fragments.TrackListNotepad;
import de.jensnistler.routemap.helper.TabManager;
import de.jensnistler.routemap.helper.TrackAdapter;
import de.jensnistler.routemap.helper.TrackDataSource;
import de.jensnistler.routemap.helper.TrackListUpdater;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

public class LoadTrack extends FragmentActivity {
    private TabHost mTabHost;
    private TabManager mTabManager;
    private TrackDataSource mDataSource;
    private String mPreferenceUser;
    private Long mPreferenceUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataSource = new TrackDataSource(this);
        mDataSource.open();

        // get preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceUser = prefs.getString("gpsiesUsername", null);
        mPreferenceUpdate = prefs.getLong("gpsiesLastUpdate", 0);

        // no gpsies user set
        if (mPreferenceUser == null || mPreferenceUser.trim().length() == 0) {
            Toast.makeText(this, R.string.missingGpsiesUsername, Toast.LENGTH_LONG).show();
            Intent preferencesActivity = new Intent(this, Preferences.class);
            startActivity(preferencesActivity);
        }
        // load data for the first time
        else if (mPreferenceUser != null && mPreferenceUpdate == 0) {
            updateTrackList();
        }

        setContentView(R.layout.fragment_tabs);
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);

        mTabManager.addTab(
            mTabHost.newTabSpec("tracks").setIndicator(getResources().getString(R.string.myTracks)),
            TrackListOwn.class,
            null
        );
        mTabManager.addTab(
            mTabHost.newTabSpec("notepad").setIndicator(getResources().getString(R.string.notepad)),
            TrackListNotepad.class,
            null
        );

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
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

    public void updateTrackList() {
        // clear list
        mDataSource.deleteAll();

        // get fresh data for tracklist and notepad
        TrackAdapter adapterTracks = ((TrackListAbstract) getSupportFragmentManager().findFragmentByTag("tracks")).getAdapter();
        new TrackListUpdater(this, mDataSource, adapterTracks, TrackListUpdater.URL_TRACKS).execute(mPreferenceUser);
        TrackAdapter adapterNotepad = ((TrackListAbstract) getSupportFragmentManager().findFragmentByTag("notepad")).getAdapter();
        new TrackListUpdater(this, mDataSource, adapterNotepad, TrackListUpdater.URL_NOTEPAD).execute(mPreferenceUser);
    }
}
