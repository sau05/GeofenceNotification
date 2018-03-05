package com.saurabh.geofence_notification.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.saurabh.geofence_notification.model.GeoItem;

/**
 * Created by kiris on 3/4/2018.
 */

public class SQLiteDataProvider {

    private SQLiteHelper helper;
    private Context mContext;
    public SQLiteDataProvider(Context context,SQLiteHelper sqLiteHelper){
        this.mContext=context;
        this.helper=sqLiteHelper;
    }

    public void saveGeofence(GeoItem geoItem){
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("lat",geoItem.getLatitude());
        cv.put("longitude",geoItem.getLongitude());
        cv.put("radius",geoItem.getRadius());
        cv.put("inside",geoItem.isIn()?1:0);
        cv.put("outside",geoItem.isOut()?1:0);
        db.insertWithOnConflict("tbl_geo",null,cv,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public GeoItem getGeofence(){
        SQLiteDatabase db=helper.getReadableDatabase();
        GeoItem geoItem=null;
        Cursor c=null;
        try {
            c=db.query("tbl_geo",new String[]{"lat","longitude","radius","inside","outside"},null,null,null,null,null);
            if (c!=null){
                if (c.moveToFirst()){
                    geoItem=new GeoItem();
                    geoItem.setLatitude(c.getDouble(c.getColumnIndex(("lat"))));
                    geoItem.setLongitude(c.getDouble(c.getColumnIndex("longitude")));
                    geoItem.setRadius(c.getDouble(c.getColumnIndex("radius")));
                    geoItem.setIn(c.getInt(c.getColumnIndex("inside"))==1);
                    geoItem.setOut(c.getInt(c.getColumnIndex("outside"))==1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (c!=null){
                c.close();
            }
        }
        return geoItem;
    }
}
