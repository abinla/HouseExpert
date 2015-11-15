package com.example.abinla.houseexpert;

/**
 * Created by abinla on 2015/11/3.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HouseDBAdaptor {

    private HouseDBHelper dbHelper;
    private Context ourcontext;
    private SQLiteDatabase database;

    public HouseDBAdaptor(Context c) {
        ourcontext = c;
    }

    public HouseDBAdaptor open() throws SQLException {
        dbHelper = new HouseDBHelper(ourcontext);
        database = dbHelper.getWritableDatabase();
        return this;

    }

    public void close() {
        dbHelper.close();
    }

    public long insert(HouseInfo house) {
        long result = -1;
        Cursor cursor;
        String address = house.getAddress();
        if(address==null||address.length()==0) return result;

        ContentValues contentValue = new ContentValues();
        contentValue.put(HouseDBHelper.ADDRESS,house.getAddress());
        contentValue.put(HouseDBHelper.SUB,house.getSub());
        contentValue.put(HouseDBHelper.PRICE,house.getPrice());
        contentValue.put(HouseDBHelper.STRUCTURE,house.getStructure());
        contentValue.put(HouseDBHelper.SALEDATE,house.getSaledate());
        contentValue.put(HouseDBHelper.LATLNG,house.getLatlng());


        cursor = searchInAddress(address);
        if (cursor.moveToFirst() == false || cursor.getCount() == 0) {
            result = database.insert(HouseDBHelper.TABLE_NAME, null, contentValue);
            if(result != -1)
            {
                Log.d("Robin", "insert" + house.toString() + "Success!");
            }else
            {
                Log.d("Robin", "insert" + house.toString() + "Fail!");
            }

        } else {
            //TODO if the info is the same, skip the update
            update(cursor.getString(cursor.getColumnIndex(HouseDBHelper._ID)), house);
        }
        cursor.close();
        return result;
    }

    public Cursor fetch() {
        String[] columns = new String[] { HouseDBHelper._ID,
                HouseDBHelper.ADDRESS,
                HouseDBHelper.SUB,
                HouseDBHelper.PRICE,
                HouseDBHelper.STRUCTURE,
                HouseDBHelper.SALEDATE,
                HouseDBHelper.LATLNG

        };

        Cursor cursor = database.query(HouseDBHelper.TABLE_NAME, columns, null,
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(String _id, HouseInfo house) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(HouseDBHelper.ADDRESS,house.getAddress());
        contentValue.put(HouseDBHelper.SUB,house.getSub());
        contentValue.put(HouseDBHelper.PRICE,house.getPrice());
        contentValue.put(HouseDBHelper.STRUCTURE,house.getStructure());
        contentValue.put(HouseDBHelper.SALEDATE,house.getSaledate());
        contentValue.put(HouseDBHelper.LATLNG,house.getLatlng());
        int updateResult = database.update(HouseDBHelper.TABLE_NAME, contentValue,
                HouseDBHelper._ID + " = " + _id, null);

        if(updateResult > 0 )
        {
            Log.d("Robin", "update" + house.toString() + "Success!");
        }else
        {
            Log.d("Robin", "update" + house.toString() + "Fail!");
        }

        return updateResult;
    }

    public Cursor searchInAddress(String address)
    {
        if (address == null || address.length() == 0) return null;
        String[] columns = new String[] { HouseDBHelper._ID,
                HouseDBHelper.ADDRESS,
                HouseDBHelper.SUB,
                HouseDBHelper.PRICE,
                HouseDBHelper.STRUCTURE,
                HouseDBHelper.SALEDATE,
                HouseDBHelper.LATLNG
        };



/*
    //following search the exactly string
    String whereClause = HouseDBHelper.ADDRESS + " = ?" ;


        String[] whereArgs = new String[] {
                address,
        };

        Cursor cursor = database.query(HouseDBHelper.TABLE_NAME, columns, whereClause,
                whereArgs, null, null, null);*/
        //following search the sub string
        String whereClause = HouseDBHelper.ADDRESS + " LIKE '%" + address + "%'";
        Cursor cursor = database.query(HouseDBHelper.TABLE_NAME, columns, whereClause,
                null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

        public Cursor searchInSub(String sub)
    {
        String saleDate;
        if (sub == null || sub.length() == 0) return null;
        String[] columns = new String[] { HouseDBHelper._ID,
                HouseDBHelper.ADDRESS,
                HouseDBHelper.SUB,
                HouseDBHelper.PRICE,
                HouseDBHelper.STRUCTURE,
                HouseDBHelper.SALEDATE,
                HouseDBHelper.LATLNG
        };
        String whereClause = HouseDBHelper.SUB + " = ?" ;
        String[] whereArgs = new String[] {
                sub,
        };
        Cursor cursor = database.query(HouseDBHelper.TABLE_NAME, columns, whereClause,
                whereArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
/*            //further filter the info by Period
            do{

                saleDate = cursor.getString(cursor.getColumnIndex(dbHelper.SALEDATE));
                if(inPeriod(saleDate,period) == false ) //remove the item not in the period
                {
                   // cursor.deleteRow();
                }

            }while(cursor.moveToNext()!=false);*/
        }
        return cursor;
    }


    public int deleteInAddress(String address) //to be finished
    {
        String whereClause = HouseDBHelper.ADDRESS + " = ?" ;
        String[] whereArgs = new String[] {
                address,
        };

        int deletedNunmber=database.delete(HouseDBHelper.TABLE_NAME,whereClause,whereArgs)    ;
        return deletedNunmber;
    }

    public void delete(long _id) {
        database.delete(HouseDBHelper.TABLE_NAME, HouseDBHelper._ID + "=" + _id, null);
    }
    public long QueryNumEntries() {
        return DatabaseUtils.queryNumEntries(database, HouseDBHelper.TABLE_NAME);
    }
}