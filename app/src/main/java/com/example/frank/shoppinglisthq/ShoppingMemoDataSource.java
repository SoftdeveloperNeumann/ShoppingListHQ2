package com.example.frank.shoppinglisthq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
//TODO fertige Einkauslisten speichern und importieren
public class ShoppingMemoDataSource {
    private static final String LOG_TAG = ShoppingMemoDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private ShoppingMemoDbHelper dbHelper;

    private String[]columns = {
            ShoppingMemoDbHelper.COLUMN_ID,
            ShoppingMemoDbHelper.COLUMN_PRODUCT,
            ShoppingMemoDbHelper.COLUMN_QUANTITY,
            ShoppingMemoDbHelper.COLUMN_CHECKED,
            ShoppingMemoDbHelper.COLUMN_PRICE,
            ShoppingMemoDbHelper.COLUMN_UNIT
    };

    public ShoppingMemoDataSource(Context context){
        Log.d(LOG_TAG, "Datasource erzeugt dbHelper");
        dbHelper = new ShoppingMemoDbHelper(context);
    }

    public void open(){
        Log.d(LOG_TAG, "Referenz auf DB wird angefragt");
        database = dbHelper.getWritableDatabase();
        database.setForeignKeyConstraintsEnabled(true);
        Log.d(LOG_TAG, "DB-Referenz erhalten. Pfad zu DB: " + database.getPath());
    }

    public void close(){
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank durch DbHelper geschlossen");
    }

    public ShoppingMemo createShoppingMemo(String product, int quantity,double price, int unit){
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT,product);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY,quantity);
        values.put(ShoppingMemoDbHelper.COLUMN_PRICE,price);
        values.put(ShoppingMemoDbHelper.COLUMN_UNIT,unit);



        long insertId = database.insert(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,null,values);
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + insertId,
                null,null,null,null);
        cursor.moveToFirst(); //TODO evtl.prüfen ob Cursor Inhalt hat!
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        cursor.close();
        return shoppingMemo;
    }

    private ShoppingMemo cursorToShoppingMemo(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_ID);
        int idProduct = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_PRODUCT);
        int idQuantity = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_QUANTITY);
        int idChecked = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_CHECKED);
        int idPrice = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_PRICE);
        int idUnit = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_UNIT);
        
        String product = cursor.getString(idProduct);
        int quantity = cursor.getInt(idQuantity);
        long id = cursor.getLong(idIndex);
        int intValueChecked = cursor.getInt(idChecked);
        int intUnit = cursor.getInt(idUnit);
        Cursor unitCursor = database.query(ShoppingMemoDbHelper.TABLE_UNITS,new String[]{"unit"},
                ShoppingMemoDbHelper.COLUMN_ID + "=" + intUnit,
                null,null,null,null);
        unitCursor.moveToFirst();
        String unit = unitCursor.getString(unitCursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_UNIT));
        double price = cursor.getDouble(idPrice);

        boolean isChecked = intValueChecked != 0;

        ShoppingMemo shoppingMemo = new ShoppingMemo(id,quantity, product,isChecked,unit,price);
        return shoppingMemo;
    }

    public ShoppingMemo updateShoppingMemo(long id, String newProduct, int newQuantity, boolean newChecked){
        int intValueChecked = newChecked? 1 : 0;
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT,newProduct);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY,newQuantity);
        values.put(ShoppingMemoDbHelper.COLUMN_CHECKED, intValueChecked);

        database.update(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,values,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id,null);
        Log.d(LOG_TAG, "updateShoppingMemo: ");
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,columns,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id,
                null,null,null,null);
        cursor.moveToFirst();
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        cursor.close();
        return shoppingMemo;
    }

    public void deleteShoppingMemo(ShoppingMemo shoppingMemo){
        long id = shoppingMemo.getId();
        database.delete(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,ShoppingMemoDbHelper.COLUMN_ID + "=" + id,
                null);
        Log.d(LOG_TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + shoppingMemo.toString());
    }

    public List<ShoppingMemo> getAllShoppingMemos(){
        List<ShoppingMemo> shoppingMemoList = new ArrayList<>();

        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,columns,
                null,null,null,null,null);

        cursor.moveToFirst();
        ShoppingMemo shoppingMemo;
        while (!cursor.isAfterLast()){
            shoppingMemo = cursorToShoppingMemo(cursor);
            shoppingMemoList.add(shoppingMemo);
            Log.d(LOG_TAG, "ID: " + shoppingMemo.getId() + ", Inhalt: " + shoppingMemo.toString());
            cursor.moveToNext();
        }
        cursor.close();
        return shoppingMemoList;
    }

    public List<String> getAllUnits() {
        List<String>units = new ArrayList<>();
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_UNITS,new String[]{"unit"},null,
                null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            units.add(cursor.getString(cursor.getColumnIndex("unit")));
            cursor.moveToNext();
        }
        cursor.close();
        return units;
    }
}
//Select product,quantity,units.unit,price from shopping_list join units on shopping_list.unit = units._id