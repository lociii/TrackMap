package de.jensnistler.routemap.helper;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TrackDataSource {
    // database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDbHelper;
    private String[] allColumns = {
        DatabaseHelper.colKey,
        DatabaseHelper.colDescription,
        DatabaseHelper.colLink,
        DatabaseHelper.colLength
    };

    public TrackDataSource(Context context) {
        mDbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public Boolean saveTrack(TrackModel track) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.colKey, track.getKey());
        values.put(DatabaseHelper.colDescription, track.getDescription());
        values.put(DatabaseHelper.colLink, track.getLink());
        values.put(DatabaseHelper.colLength, track.getLength());

        // check if dataset exists
        Cursor cursor = mDatabase.query(DatabaseHelper.trackTable, allColumns, "Key=?", new String[] { track.getKey() }, null, null, null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            values.remove(DatabaseHelper.colKey);
            if (1 == mDatabase.update(DatabaseHelper.trackTable, values, "Key = ?", new String[] { track.getKey() })) {
                return true;
            }
        }
        else {
            if (-1 != mDatabase.insert(DatabaseHelper.trackTable, null, values)) {
                return true;
            }
        }

        return false;
    }

    public TrackModel getTrack(String Key) {
        Cursor cursor = mDatabase.query(DatabaseHelper.trackTable, allColumns, "Key=?", new String[] { Key }, null, null, null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            return cursorToTrack(cursor);
        }
        return null;
    }

    public ArrayList<TrackModel> getAllTracks() {
        ArrayList<TrackModel> tracks = new ArrayList<TrackModel>();

        Cursor cursor = mDatabase.query(DatabaseHelper.trackTable, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TrackModel track = cursorToTrack(cursor);
            tracks.add(track);
            cursor.moveToNext();
        }
        cursor.close();

        return tracks;
    }

    private TrackModel cursorToTrack(Cursor cursor) {
        TrackModel track = new TrackModel(cursor.getString(0));
        track.setDescription(cursor.getString(1));
        track.setLink(cursor.getString(2));
        track.setLength(cursor.getFloat(3));

        return track;
    }

    public void deleteAll() {
        mDatabase.execSQL("DELETE FROM " + DatabaseHelper.trackTable);
    }
}
