package com.example.frank.shoppinglisthq;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ShoppingMemoDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ShoppingMemoDbHelper.class.getSimpleName();

    public static final String DB_NAME = "shopping_list.db";
    public static final int DB_VERSION = 3;

    public static final String TABLE_SHOPPING_LIST = "shopping_list";
    public static final String TABLE_UNITS = "units";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRODUCT = "product";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_CHECKED = "checked";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_UNIT = "unit";

    public static final String SQL_CREATE_UNITS =
            "CREATE TABLE " + TABLE_UNITS +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_UNIT + " TEXT NOT NULL);";

    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_SHOPPING_LIST +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT + " TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                    COLUMN_CHECKED + " BOOLEAN NOT NULL DEFAULT 0, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_UNIT + " INTEGER, " +
                    "FOREIGN KEY("+ COLUMN_UNIT + ")REFERENCES "+ TABLE_UNITS + "("+ COLUMN_ID + "));";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_SHOPPING_LIST;


    public ShoppingMemoDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        Log.d(LOG_TAG, "Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(LOG_TAG, "Tabelle wird mit " + SQL_CREATE + " angelegt");

            db.execSQL(SQL_CREATE_UNITS);
            db.execSQL("INSERT INTO " + TABLE_UNITS + "(" + COLUMN_UNIT +")VALUES " +
            "('g'),('Kg'),('l'),('Stk'),('Pckg')");
            db.execSQL(SQL_CREATE);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        onCreate(db);

    }
}
