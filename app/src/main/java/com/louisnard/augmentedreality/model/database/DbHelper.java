package com.louisnard.augmentedreality.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.louisnard.augmentedreality.model.objects.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing the {@link SQLiteDatabase}.
 *
 * @author Alexandre Louisnard
 */

public class DbHelper extends SQLiteOpenHelper {

    // Tag
    private static final String TAG = DbHelper.class.getSimpleName();

    // Database information
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton pattern
    private static DbHelper sInstance;

    // SQL requests
    private static final String SQL_CREATE_TABLE_POINTS = "CREATE TABLE " + DbContract.PointsColumns.TABLE_NAME
            + " (" + DbContract.PointsColumns._ID + " INTEGER PRIMARY KEY,"
            + DbContract.PointsColumns.COLUMN_NAME + " TEXT,"
            + DbContract.PointsColumns.COLUMN_LATITUDE + " REAL,"
            + DbContract.PointsColumns.COLUMN_LONGITUDE + " REAL,"
            + DbContract.PointsColumns.COLUMN_ELEVATION + " INTEGER)";

    /**
     * Constructs a new instance of {@link DbHelper}.
     * Private constructor to prevent accidental instantiation.
     * @param applicationContext the {@link Context} to use to open or create the database.
     */
    private DbHelper(Context applicationContext) {
        super(applicationContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Initializes if necessary and returns the singleton instance of {@link DbHelper}.
     * @param applicationContext the application context to avoid leaking an activity context.
     * @return the singleton instance of {@link DbHelper}.
     */
    public static synchronized DbHelper getInstance(Context applicationContext) {
        if (sInstance == null) {
            sInstance = new DbHelper(applicationContext);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_POINTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do for the moment
    }

    /**
     * Static method that returns the database name.
     * @return the database name.
     */
    public static String getDbName() {
        return DATABASE_NAME;
    }

    /**
     * Clears the specified table.
     * @param tableName the table name.
     */
    public void clearTable(String tableName) {
        final SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + tableName);
        db.close();
    }

    /**
     * Gets the {@link List<Point>} of all the points from the {@link SQLiteDatabase}.
     * @return the {@link List<Point>}.
     */
    public List<Point> getAllPoints() {
        final SQLiteDatabase db = getWritableDatabase();
        final Cursor cursor = db.query(DbContract.PointsColumns.TABLE_NAME, null, null, null, null, null, null);
        final List<Point> points = new ArrayList<>();
        while (cursor.moveToNext()) {
            points.add(new Point(cursor));
        }
        cursor.close();
        db.close();
        return points;
    }

    /**
     * Gets the {@link List<Point>} of all points from the {@link SQLiteDatabase} around the specified {@link Point}.
     * Actually, the points are located in a square of size 2x{@param distance} and centered on the specified {@param point}.
     * @param point the {@link Point} around which the points have to be located.
     * @param distance the half-size of the square around the {@link Point} where the points have to be located.
     * @return the {@link List<Point>} of all points located around the specified {@link Point}.
     */
    public List<Point> getPointsAround(Point point, long distance) {
        // Delimitate the square within which to find points
        final String latMin = String.valueOf((point.getLatitude() - Point.metersToDegrees(distance)) % 360);
        final String latMax = String.valueOf((point.getLatitude() + Point.metersToDegrees(distance)) % 360);
        final String lonMin = String.valueOf((point.getLongitude() - Point.metersToDegrees(distance)) % 360);
        final String lonMax = String.valueOf((point.getLongitude() + Point.metersToDegrees(distance)) % 360);
        // Read database
        final SQLiteDatabase db = getWritableDatabase();
        final Cursor cursor = db.query(DbContract.PointsColumns.TABLE_NAME, null,
                DbContract.PointsColumns.COLUMN_LATITUDE + " >= ? AND " + DbContract.PointsColumns.COLUMN_LATITUDE + " <= ? AND " + DbContract.PointsColumns.COLUMN_LONGITUDE + " >= ? AND " + DbContract.PointsColumns.COLUMN_LONGITUDE + " <= ?",
                new String[] {latMin, latMax, lonMin, lonMax}, null, null, null);
        final List<Point> points = new ArrayList<>();
        while (cursor.moveToNext()) {
            points.add(new Point(cursor));
        }
        cursor.close();
        db.close();
        return points;
    }

    /**
     * Adds the specified {@link Point} to the {@link SQLiteDatabase}.
     * @param point the {@link Point} to insert.
     * @return the row id of the newly inserted row, or -1 if an error occurred.
     */
    public long addPoint(Point point) {
        final SQLiteDatabase db = getWritableDatabase();
        final long result = insertPoint(point, db);
        db.close();
        return result;
    }

    /**
     *
     * Adds the specified {@link List<Point>} to the {@link SQLiteDatabase}.
     * @param points the {@link List<Point>} to insert.
     * @return the number of successfully inserted rows, or -1 if an error occurred on one or many rows.
     */
    public long addPoints(List<Point> points) {
        final SQLiteDatabase db = getWritableDatabase();
        long result = 0;
        for (Point point : points) {
            if (insertPoint(point, db) != -1 && result != -1) {
                result++;
            } else {
                result = -1;
            }
        }
        db.close();
        return result;
    }

    /**
     * Inserts a {@link Point} in the specified {@link SQLiteDatabase}.
     * The {@link SQLiteDatabase} must be closed after calling this function.
     * @param point the {@link List<Point>} to insert.
     * @param db the {@link SQLiteDatabase} to insert the point into.
     * @return the row id of the newly inserted row, or -1 if an error occurred.
     */
    private long insertPoint(Point point, SQLiteDatabase db) {
        final ContentValues values = new ContentValues();
        values.put(DbContract.PointsColumns.COLUMN_NAME, point.getName());
        values.put(DbContract.PointsColumns.COLUMN_LATITUDE, point.getLatitude());
        values.put(DbContract.PointsColumns.COLUMN_LONGITUDE, point.getLongitude());
        values.put(DbContract.PointsColumns.COLUMN_ELEVATION, point.getElevation());
        final long result = db.insert(DbContract.PointsColumns.TABLE_NAME, null, values);
        if (result == -1) {
            Log.d(TAG, "Error inserting the point: \"" + point.getName() + "\" into the database.");
        }
        return result;
    }
}