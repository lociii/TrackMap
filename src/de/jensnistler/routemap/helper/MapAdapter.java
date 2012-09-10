package de.jensnistler.routemap.helper;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.jensnistler.routemap.R;

public class MapAdapter extends ArrayAdapter<MapModel> {
    private ArrayList<MapModel> mItems;
    private Context mContext;

    public MapAdapter(Context context, int textViewResourceId, ArrayList<MapModel> items) {
        super(context, textViewResourceId, items);

        mItems = items;
        mContext = context;

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
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.manage_maps_row, null);
        }

        MapModel map = mItems.get(position);
        if (map != null) {
            ImageView ic = (ImageView) v.findViewById(R.id.icon);
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
            if (tt != null) {
                tt.setText(map.getDescription());
            }
            if (bt != null){
                bt.setText(map.getSize() + " MB");
            }
            if (ic != null) {
                if (0 == map.getUpdated()) {
                    ic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.map_add));
                }
                else {
                    if (map.getUpdated() >= map.getDate()) {
                        ic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.map_ok));
                    }
                    else {
                        ic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.update));
                    }
                }
            }
        }
        return v;
    }
}
