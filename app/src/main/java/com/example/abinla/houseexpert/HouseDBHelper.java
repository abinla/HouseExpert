package com.example.abinla.houseexpert;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by abinla on 2015/11/3.
 */
public class HouseDBHelper extends SQLiteOpenHelper {

    // Database Information
    static final String DB_NAME = "HOUSEINFO.DB";

    // Table Name
    public static final String TABLE_NAME = "HOUSE";

    // Table columns
    public static final String _ID = "_id";
    public static final String ADDRESS = "address";
    public static final String SUB = "sub";
    public static final String PRICE = "price";
    public static final String STRUCTURE = "structure";
    public static final String SALEDATE = "saledate";
    public static final String LATLNG = "latlng";



    // database version
    static final int DB_VERSION = 2; //add SUB colume

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ADDRESS + " TEXT NOT NULL UNIQUE, "
            + SUB + " TEXT NOT NULL, "
            + PRICE + " TEXT NOT NULL, "
            + STRUCTURE + " TEXT NOT NULL, "
            + SALEDATE + " TEXT NOT NULL, "
            + LATLNG + " TEXT);";

    public HouseDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}