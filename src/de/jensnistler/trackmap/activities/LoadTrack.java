package de.jensnistler.trackmap.activities;

import de.jensnistler.trackmap.R;
import de.jensnistler.trackmap.fragments.TrackListAbstract;
import de.jensnistler.trackmap.fragments.TrackListNotepad;
import de.jensnistler.trackmap.fragments.TrackListOwn;
import de.jensnistler.trackmap.helper.TabManager;
import de.jensnistler.trackmap.helper.TrackAdapter;
import de.jensnistler.trackmap.helper.TrackDataSource;
import de.jensnistler.trackmap.helper.TrackListUpdater;
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

        TrackListAbstract tracksFragment = (TrackListAbstract) getSupportFragmentManager().findFragmentByTag("tracks");
        TrackAdapter tracksAdapter = null;
        if (null != tracksFragment) {
            tracksAdapter = tracksFragment.getAdapter();
        }
        new TrackListUpdater(this, mDataSource, tracksAdapter, TrackListUpdater.URL_TRACKS).execute(mPreferenceUser);

        TrackListAbstract notepadFragment = (TrackListAbstract) getSupportFragmentManager().findFragmentByTag("notepad");
        TrackAdapter notepadAdapter = null;
        if (null != notepadFragment) {
            notepadAdapter = notepadFragment.getAdapter();
        }
        new TrackListUpdater(this, mDataSource, notepadAdapter, TrackListUpdater.URL_NOTEPAD).execute(mPreferenceUser);
    }
}
