package de.jensnistler.trackmap.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String dbName = "TrackMap";
    public static final String mapTable = "Maps";
    public static final String trackTable = "Tracks";
    public static final String colKey = "Key";
    public static final String colDescription = "Description";
    public static final String colDate = "Date";
    public static final String colSize = "Size";
    public static final String colUrl = "Url";
    public static final String colUpdated = "Updated";
    public static final String colLength = "Length";
    public static final String colLink = "Link";
    public static final String colType = "Type";

    public DatabaseHelper(Context context) {
        super(context, dbName, null, 1); 
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + mapTable + " ("
            + colKey + " TEXT PRIMARY KEY, "
            + colDescription + " TEXT, "
            + colDate + " INTEGER, "
            + colSize + " REAL, "
            + colUrl + " TEXT, "
            + colUpdated + " INTEGER "
            + ");"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS " + trackTable + " ("
                + colKey + " TEXT PRIMARY KEY, "
                + colDescription + " TEXT, "
                + colLink + " TEXT, "
                + colLength + " REAL, "
                + colType + " INTEGER "
                + ");"
            );
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }
}
