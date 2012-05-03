package de.jensnistler.routemap.activities;

import java.io.File;
import java.util.ArrayList;

import de.jensnistler.routemap.helper.FilenameFilterRoutes;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LoadRouteFromSD extends ListActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all files on sd card
        File files[] = Environment.getExternalStorageDirectory().listFiles(new FilenameFilterRoutes());
        ArrayList<String> hits = new ArrayList<String>();
        recursiveFileFind(files, hits);

        // convert to string array
        String[] filenames = new String[hits.size()];
        Object hitsArray[] = hits.toArray();
        for (int i = 0; i < hitsArray.length; i++) {
            filenames[i] = (String) hitsArray[i];
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            filenames
        );
        setListAdapter(adapter);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        String selection = l.getItemAtPosition(position).toString();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("routeFile", selection);
        editor.commit();

        Intent mapActivity = new Intent(getBaseContext(), Map.class);
        startActivity(mapActivity);
    }

    public void recursiveFileFind(File[] source, ArrayList<String> hits){
        int i = 0;
        String path = "";
        if (source != null) {
            while (i != source.length) {
                path = source[i].getAbsolutePath();

                if (source[i].isDirectory()){
                    File file[] = source[i].listFiles(new FilenameFilterRoutes());
                    recursiveFileFind(file, hits);
                }
                else {
                    hits.add(path);
                }

                i++;
            }
        }
    }
}
