package edu.asu.cse535assgn1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.asu.cse535assgn1.models.Accelerometer;

/**
 * Handles all database related functionalities.
 *
 * Created by Jithin Roy on 3/4/16.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private DBHelper mHelper;
    private Context context;

    private String TAG = "DatabaseManager";
    private String SensorTAG = "SENSOR";

    private String accelorometerTableName = "";

    private DatabaseManager() {
        // Added to avoid multiple instances
        // This is a singleton
    }

    public static DatabaseManager sharedInstance() {
        if(instance == null) {
            instance = new DatabaseManager();

        }
        return instance;
    }

    private void initializeDB(String tableName, Context context) {
        this.context = context;
        mHelper = new DBHelper(context, tableName);
    }


    //==============================================================================
    //                       Public methods
    //==============================================================================


    public boolean isDBAvialable() {
        return (mHelper != null);
    }
    /**
     * Saves the list of accelerometer data to database.
     *
     * @param list
     */
    public void saveAccelerometerList(List<Accelerometer> list) {

        SQLiteDatabase db = openDatabase();

        for (Accelerometer acc: list) {

            ContentValues values = new ContentValues();
            values.put(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_TIME_STAMP, acc.getTimestamp());
            values.put(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_X_VALUE, acc.getX());
            values.put(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_Y_VALUE, acc.getY());
            values.put(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_Z_VALUE, acc.getZ());


            long newRowId;
            newRowId = db.insert("AccelerometerTable",
                    null,
                    values);

        }
        db.close();

    }

    /**
     * Returns a list of recent 'count' of accelerometer data from database.
     *
     * @param count Number of records to be fetched.
     * @return List of accelerometer values.
     */
    public List<Accelerometer> fetchRecentAccelerometerData(int count) {
        SQLiteDatabase db = openDatabase();
        List<Accelerometer> result = fetchRecentAccelerometerValuesFromDB(db, count);
        db.close();
        return result;
    }

    /**
     * Returns a list of recent 'count' of accelerometer data from the given database.
     *
     * @param count Number of records to be fetched.
     * @return List of accelerometer values.
     */
    public List<Accelerometer> fetchRecentAccelerometerData(String dbPath, int count) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        List<Accelerometer> result = fetchRecentAccelerometerValuesFromDB(db, count);
        db.close();
        return result;
    }

    private List<Accelerometer> fetchRecentAccelerometerValuesFromDB(SQLiteDatabase db, int count) {
        String limit = Integer.toString(count);
        String[] projection = {
                AccelerometerContract.AccelerometerEntry._ID,
                AccelerometerContract.AccelerometerEntry.COLUMN_NAME_TIME_STAMP,
                AccelerometerContract.AccelerometerEntry.COLUMN_NAME_X_VALUE,
                AccelerometerContract.AccelerometerEntry.COLUMN_NAME_Y_VALUE,
                AccelerometerContract.AccelerometerEntry.COLUMN_NAME_Z_VALUE,

        };
        Cursor cursor = db.query(
                "AccelerometerTable",
                projection,
                null,
                null,
                null,
                null,
                AccelerometerContract.AccelerometerEntry.COLUMN_NAME_TIME_STAMP+" DESC",
                limit
        );

        List<Accelerometer> result = new ArrayList<>();


        while (cursor.moveToNext()) {
            Accelerometer acc = new Accelerometer();

            long timestamp = cursor.getLong(
                    cursor.getColumnIndexOrThrow(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_TIME_STAMP)
            );
            float x = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_X_VALUE)
            );
            float y = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_Y_VALUE)
            );
            float z = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(AccelerometerContract.AccelerometerEntry.COLUMN_NAME_Z_VALUE)
            );
            acc.setTimestamp(timestamp);
            acc.setX(x);
            acc.setY(y);
            acc.setZ(z);
            result.add(acc);

        }

        cursor.close();
        db.close();
        return result;
    }

    /**
     * Returns the location where the database file is currently stored.
     *
     * @return Absolute path of database location.
     */
    public String databaseAbsolutePath() {
        File file = context.getDatabasePath(accelorometerTableName);
        Log.i(TAG, "DB path = " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public String databaseName() {
        File file = context.getDatabasePath(accelorometerTableName);
        Log.i(TAG, "DB file name = " + file.getAbsolutePath());
        return file.getName();
    }

    public void createTable(String tableName, Context context) {
        mHelper = null;
        accelorometerTableName = tableName;
        initializeDB(tableName, context);
    }

    //==============================================================================
    //                          Internals
    //==============================================================================

    private SQLiteDatabase openDatabase() {
        if (mHelper != null) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            return db;
        }
        return null;


    }

    private void closeDatabase(SQLiteDatabase db) {
        if (db != null) db.close();
    }

}
