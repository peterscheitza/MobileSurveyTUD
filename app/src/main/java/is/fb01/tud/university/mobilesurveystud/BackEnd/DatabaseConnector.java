package is.fb01.tud.university.mobilesurveystud.BackEnd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import is.fb01.tud.university.mobilesurveystud.GlobalSettings;

/**
 * Created by peter_000 on 07.06.2015.
 */
public class DatabaseConnector {

    static final public String TAG = "DatabaseConnector";

    Context context;
    SQLiteDatabase db;

    public static final String DATABASE_NAME = "MobileSurveysDB";
    public static final String TABLE_NAME = "ExceptionalApps";
    public static final int DATABASE_VERSION = 1;

    public DatabaseConnector(Context context) {
        this.context = context;
        DatabaseHelper openHelper = new DatabaseHelper(this.context);
        this.db = openHelper.getWritableDatabase();
    }

    public void insert(String appName){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NAME_ROW, appName);

        db.insert(
                TABLE_NAME,
                "null",
                values);

        Log.v(TAG, "insert into database: " + appName);
    }

    public Vector<String> readAllEntrys(){
        String[] colums = {
                DatabaseHelper.PRIM_KEY,
                DatabaseHelper.NAME_ROW
        };

        Cursor dataCursor = db.query(
                TABLE_NAME,  // The table to query
                colums,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        Vector<String> toReturn = new Vector<>();

        if (dataCursor.moveToFirst()) {
            do {
                String current = dataCursor.getString( dataCursor.getColumnIndexOrThrow(DatabaseHelper.NAME_ROW));
                toReturn.add(current);

                Log.v(TAG, "read from database: " + current);
            } while (dataCursor.moveToNext());
        }

        return toReturn;
    }

    private class DatabaseHelper extends SQLiteOpenHelper{

        public static final String PRIM_KEY = "id";
        public static final String NAME_ROW = "app_name";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG, "create database");
            db.execSQL("CREATE TABLE "
                    + TABLE_NAME
                    + "( " + PRIM_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + NAME_ROW + " VARCHAR(512) )");

            insertDefaultExceptions(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        private void insertDefaultExceptions(SQLiteDatabase freshDB){

            //for(int i = 0; i < GlobalSettings.gDefaultExceptionalApps.length; i++){ //normal : iterate funzt net
            for(String current :  GlobalSettings.gDefaultExceptionalApps){
                //String current = GlobalSettings.gDefaultExceptionalApps[i];

                freshDB.execSQL("INSERT OR REPLACE INTO "
                        + TABLE_NAME
                        + "( " + NAME_ROW + " ) VALUES ( '" + current + "' )" );

                Log.v(TAG, "insert into database: " + current);
            }

        }
    }
}
