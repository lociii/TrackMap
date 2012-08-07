package de.jensnistler.routemap.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String dbName = "RouteMap";
    public static final String mapTable = "Maps";
    public static final String colKey = "Key";
    public static final String colDescription = "Description";
    public static final String colDate = "Date";
    public static final String colSize = "Size";
    public static final String colUrl = "Url";
    public static final String colUpdated = "Updated";
    
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
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }
}
