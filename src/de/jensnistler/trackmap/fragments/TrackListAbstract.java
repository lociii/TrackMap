package de.jensnistler.trackmap.fragments;

import java.io.File;

import de.jensnistler.trackmap.R;
import de.jensnistler.trackmap.activities.Map;
import de.jensnistler.trackmap.helper.TrackAdapter;
import de.jensnistler.trackmap.helper.TrackDataSource;
import de.jensnistler.trackmap.helper.TrackDownloader;
import de.jensnistler.trackmap.helper.TrackModel;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

abstract public class TrackListAbstract extends ListFragment {
    private TrackAdapter mAdapter;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext =  getActivity().getBaseContext();
        View view = inflater.inflate(R.layout.load_track, container, false);

        TrackDataSource dataSource = new TrackDataSource(mContext);
        dataSource.open();

        mAdapter = new TrackAdapter(
            mContext,
            android.R.layout.simple_list_item_1,
            dataSource,
            getType()
        );
        mAdapter.loadData();
        setListAdapter(mAdapter);

        return view;
    }

    public TrackAdapter getAdapter() {
        return mAdapter;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        TrackModel selection = (TrackModel) l.getItemAtPosition(position);

        // check if track has already been cached
        File cacheDir = mContext.getExternalCacheDir();
        if (null == cacheDir || !cacheDir.canRead()) {
            Toast.makeText(mContext, R.string.cannotReadFromCache, Toast.LENGTH_LONG).show();
            return;
        }

        File cacheFile = new File(cacheDir, selection.getKey() + ".gpx");
        if (cacheFile.exists()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("routeFile", cacheFile.getAbsolutePath());
            editor.commit();

            Intent mapActivity = new Intent(mContext, Map.class);
            startActivity(mapActivity);
            return;
        }

        // cache track
        new TrackDownloader(getActivity()).execute(selection);
    }

    abstract protected Integer getType();
}
