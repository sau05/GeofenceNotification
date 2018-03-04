package com.saurabh.geofence_notification.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kiris on 3/4/2018.
 */

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String TABLE_GEOFENCE_CREATE_SQL="CREATE TABLE tbl_geo (id INTEGER" +
            " PRIMARY KEY, lat REAL, longitude REAL, radius REAL NOT NULL, inside INTEGER NOT NULL," +
            " outside INTEGER NOT NULL)";

    public SQLiteHelper(Context context) {
        super(context, "geofence.db", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_GEOFENCE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
