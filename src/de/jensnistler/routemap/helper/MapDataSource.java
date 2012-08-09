package de.jensnistler.routemap.helper;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MapDataSource {
    // database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDbHelper;
    private String[] allColumns = {
        DatabaseHelper.colKey,
        DatabaseHelper.colDescription,
        DatabaseHelper.colDate,
        DatabaseHelper.colSize,
        DatabaseHelper.colUrl,
        DatabaseHelper.colUpdated
    };

    public MapDataSource(Context context) {
        mDbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public Boolean saveMap(MapModel map) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.colKey, map.getKey());
        values.put(DatabaseHelper.colDescription, map.getDescription());
        values.put(DatabaseHelper.colDate, map.getDate());
        values.put(DatabaseHelper.colSize, map.getSize());
        values.put(DatabaseHelper.colUrl, map.getUrl());
        values.put(DatabaseHelper.colUpdated, map.getUpdated());

        // check if dataset exists
        Cursor cursor = mDatabase.query(DatabaseHelper.mapTable, allColumns, "Key=?", new String[] { map.getKey() }, null, null, null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            values.remove(DatabaseHelper.colKey);
            if (1 == mDatabase.update(DatabaseHelper.mapTable, values, "Key = ?", new String[] { map.getKey() })) {
                return true;
            }
        }
        else {
            if (-1 != mDatabase.insert(DatabaseHelper.mapTable, null, values)) {
                return true;
            }
        }

        return false;
    }

    public MapModel getMap(String Key) {
        Cursor cursor = mDatabase.query(DatabaseHelper.mapTable, allColumns, "Key=?", new String[] { Key }, null, null, null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            return cursorToMap(cursor);
        }
        return null;
    }

    public ArrayList<MapModel> getAllMaps() {
        ArrayList<MapModel> maps = new ArrayList<MapModel>();

        Cursor cursor = mDatabase.query(DatabaseHelper.mapTable, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MapModel map = cursorToMap(cursor);
            maps.add(map);
            cursor.moveToNext();
        }
        cursor.close();

        return maps;
    }

    private MapModel cursorToMap(Cursor cursor) {
        MapModel map = new MapModel(cursor.getString(0));
        map.setDescription(cursor.getString(1));
        map.setDate(cursor.getInt(2));
        map.setSize(cursor.getLong(3));
        map.setUrl(cursor.getString(4));
        map.setUpdated(cursor.getInt(5));

        return map;
    }
}
