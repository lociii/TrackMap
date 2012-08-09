package de.jensnistler.routemap.helper;

import java.util.ArrayList;
import java.util.Comparator;

import de.jensnistler.routemap.R;
import de.jensnistler.routemap.activities.MapMapsForge;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrackAdapter extends ArrayAdapter<TrackModel> {
    private ArrayList<TrackModel> mItems;
    private Context mContext;
    private String mPreferenceDistanceUnit;

    public TrackAdapter(Context context, int textViewResourceId, ArrayList<TrackModel> items) {
        super(context, textViewResourceId, items);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferenceDistanceUnit = prefs.getString("distance", MapMapsForge.DISTANCE_MILES);
        mContext = context;
        mItems = items;

        sortData();
    }

    public void add(TrackModel track) {
        super.add(track);
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
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.load_tracks_row, null);
        }

        TrackModel track = mItems.get(position);
        if (track != null) {
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            if (tt != null) {
                tt.setText(track.getDescription());
            }
            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
            if (bt != null) {
                Float length = track.getLength() / 1000f;
                String unit = "km";

                // convert to miles
                if (mPreferenceDistanceUnit.equals(MapMapsForge.DISTANCE_MILES)) {
                    length = length / 1.609344f;
                    unit = "mi";
                }

                bt.setText(Math.round(length) + " " + unit);
            }
        }
        return v;
    }
}
