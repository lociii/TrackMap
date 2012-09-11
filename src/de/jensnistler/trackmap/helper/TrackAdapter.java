package de.jensnistler.trackmap.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import de.jensnistler.trackmap.R;
import de.jensnistler.trackmap.activities.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrackAdapter extends ArrayAdapter<TrackModel> {
    private Context mContext;
    private String mPreferenceDistanceUnit;
    private TrackDataSource mDataSource;
    private Integer mType;

    public TrackAdapter(Context context, int textViewResourceId, TrackDataSource dataSource, Integer type) {
        super(context, textViewResourceId, new ArrayList<TrackModel>());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferenceDistanceUnit = prefs.getString("distance", Map.DISTANCE_MILES);
        mContext = context;
        mDataSource = dataSource;
        mType = type;
    }

    public void loadData() {
        clear();
        ArrayList<TrackModel> tracks = mDataSource.getAllTracks(mType);
        Iterator<TrackModel> iterator = tracks.iterator();
        while (iterator.hasNext()) {
            TrackModel track = iterator.next();
            add(track);
        }
        sortData();

        notifyDataSetChanged();
    }

    public void add(TrackModel track) {
        super.add(track);
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
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.load_tracks_row, null);
        }
        

        TrackModel track = getItem(position);
        if (track != null) {
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            if (tt != null) {
                tt.setText(track.getDescription());
            }
            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
            if (bt != null) {
                Float length = track.getLength() / 1000f;
                String unit = mContext.getResources().getString(R.string.km);

                // convert to miles
                if (mPreferenceDistanceUnit.equals(Map.DISTANCE_MILES)) {
                    length = length / 1.609344f;
                    unit = mContext.getResources().getString(R.string.mi);
                }

                bt.setText(Math.round(length) + " " + unit);
            }
        }
        return v;
    }
}
