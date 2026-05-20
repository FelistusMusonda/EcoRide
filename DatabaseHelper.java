package com.example.ecoride;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EcoRide.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_IS_ADMIN = "is_admin";

    private static final String TABLE_TRIPS = "trips";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TRIP_DATA = "trip_data";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table with admin column
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_IS_ADMIN + " INTEGER DEFAULT 0)";
        db.execSQL(createUsersTable);

        // Create trips table
        String createTripsTable = "CREATE TABLE " + TABLE_TRIPS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_TRIP_DATA + " TEXT, " +
                COLUMN_TIMESTAMP + " TEXT)";
        db.execSQL(createTripsTable);

        // Create default admin account
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_NAME, "Admin");
        adminValues.put(COLUMN_EMAIL, "admin@ecoride.com");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_IS_ADMIN, 1);
        db.insert(TABLE_USERS, null, adminValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        onCreate(db);
    }

    // Register new user (regular user, not admin)
    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IS_ADMIN, 0);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    // Check if email exists
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Login user - returns 1=admin, 2=regular, 0=failed
    public int loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID, COLUMN_IS_ADMIN},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        int userId = -1;
        int isAdmin = 0;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
            isAdmin = cursor.getInt(1);
        }
        cursor.close();
        db.close();
        return isAdmin == 1 ? 1 : (userId != -1 ? 2 : 0);
    }

    // Get user ID by email
    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    // Get user name by email
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_NAME},
                COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }

    // Check if user is admin
    public boolean isAdmin(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_IS_ADMIN},
                COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean isAdmin = false;
        if (cursor.moveToFirst()) {
            isAdmin = cursor.getInt(0) == 1;
        }
        cursor.close();
        db.close();
        return isAdmin;
    }

    // Get all users (for admin)
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_IS_ADMIN},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            User user = new User();
            user.id = cursor.getInt(0);
            user.name = cursor.getString(1);
            user.email = cursor.getString(2);
            user.isAdmin = cursor.getInt(3) == 1;
            userList.add(user);
        }
        cursor.close();
        db.close();
        return userList;
    }

    // Save trip data for user
    public void saveTripData(int userId, String tripData, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TRIP_DATA, tripData);
        values.put(COLUMN_TIMESTAMP, timestamp);
        db.insert(TABLE_TRIPS, null, values);
        db.close();
    }

    // Get all trip data for a user
    public List<String> getUserTrips(int userId) {
        List<String> trips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRIPS, new String[]{COLUMN_TRIP_DATA, COLUMN_TIMESTAMP},
                COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, COLUMN_TIMESTAMP + " DESC");

        while (cursor.moveToNext()) {
            trips.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return trips;
    }

    // User class
    public static class User {
        public int id;
        public String name;
        public String email;
        public boolean isAdmin;
    }
}