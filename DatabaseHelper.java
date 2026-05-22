package com.example.ecoride;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EcoRide.db";
    private static final int DATABASE_VERSION = 3;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_NAME = "username";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_ROLE = "role";
    private static final String COL_TOTAL_CARBON = "total_carbon";
    private static final String COL_TOTAL_TRIPS = "total_trips";

    // Trips table
    private static final String TABLE_TRIPS = "trips";
    private static final String COL_TRIP_ID = "trip_id";
    private static final String COL_USER_ID_FK = "user_id";
    private static final String COL_USER_NAME_FK = "user_name";
    private static final String COL_TRIP_FROM = "trip_from";
    private static final String COL_TRIP_TO = "trip_to";
    private static final String COL_TRIP_MODE = "trip_mode";
    private static final String COL_TRIP_DISTANCE = "distance";
    private static final String COL_TRIP_CARBON = "carbon_saved";
    private static final String COL_TRIP_DATE = "trip_date";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_ROLE + " TEXT DEFAULT 'user', " +
                COL_TOTAL_CARBON + " REAL DEFAULT 0, " +
                COL_TOTAL_TRIPS + " INTEGER DEFAULT 0)";
        db.execSQL(createUsersTable);

        // Create trips table
        String createTripsTable = "CREATE TABLE " + TABLE_TRIPS + " (" +
                COL_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_ID_FK + " INTEGER, " +
                COL_USER_NAME_FK + " TEXT, " +
                COL_TRIP_FROM + " TEXT, " +
                COL_TRIP_TO + " TEXT, " +
                COL_TRIP_MODE + " TEXT, " +
                COL_TRIP_DISTANCE + " REAL, " +
                COL_TRIP_CARBON + " REAL, " +
                COL_TRIP_DATE + " LONG, " +
                "FOREIGN KEY(" + COL_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createTripsTable);

        // Insert default admin account
        insertDefaultAdmin(db);
    }

    private void insertDefaultAdmin(SQLiteDatabase db) {
        ContentValues adminValues = new ContentValues();
        adminValues.put(COL_USER_NAME, "Admin");
        adminValues.put(COL_USER_EMAIL, "admin@ecoride.com");
        adminValues.put(COL_USER_PASSWORD, "admin123");
        adminValues.put(COL_USER_ROLE, "admin");
        adminValues.put(COL_TOTAL_CARBON, 0);
        adminValues.put(COL_TOTAL_TRIPS, 0);
        db.insert(TABLE_USERS, null, adminValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User class
    public static class User {
        public int id;
        public String name;
        public String email;
        public String password;
        public String role;
        public double totalCarbon;
        public int totalTrips;
    }

    // Trip class
    public static class Trip {
        public int tripId;
        public int userId;
        public String userName;
        public String from;
        public String to;
        public String mode;
        public double distance;
        public double carbonSaved;
        public long date;
    }

    // Check if email already exists
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_EMAIL},
                COL_USER_EMAIL + "=?", new String[]{email}, null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    // Register new user
    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_ROLE, "user");
        values.put(COL_TOTAL_CARBON, 0);
        values.put(COL_TOTAL_TRIPS, 0);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Login user - returns User object
    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            user.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
            user.email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
            user.password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD));
            user.role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
            user.totalCarbon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_CARBON));
            user.totalTrips = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_TRIPS));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // Get user by ID
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            user.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
            user.email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
            user.role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
            user.totalCarbon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_CARBON));
            user.totalTrips = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_TRIPS));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // Get user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + "=?", new String[]{email},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            user.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
            user.email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
            user.role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
            user.totalCarbon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_CARBON));
            user.totalTrips = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_TRIPS));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // Get user name by email
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_NAME},
                COL_USER_EMAIL + "=?", new String[]{email}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }
        if (cursor != null) cursor.close();
        return "";
    }

    // Get all users (for admin)
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_ROLE + "=?", new String[]{"user"},
                null, null, COL_USER_NAME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                user.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
                user.email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
                user.role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
                user.totalCarbon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_CARBON));
                user.totalTrips = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_TRIPS));
                users.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return users;
    }

    // Get all users including admin (for admin)
    public List<User> getAllUsersIncludingAdmin() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null, null, null,
                null, null, COL_USER_NAME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                user.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
                user.email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
                user.role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
                user.totalCarbon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_CARBON));
                user.totalTrips = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_TRIPS));
                users.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return users;
    }

    // Get all trips (for admin)
    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRIPS, null, null, null,
                null, null, COL_TRIP_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Trip trip = new Trip();
                trip.tripId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID));
                trip.userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID_FK));
                trip.userName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME_FK));
                trip.from = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_FROM));
                trip.to = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_TO));
                trip.mode = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_MODE));
                trip.distance = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_DISTANCE));
                trip.carbonSaved = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_CARBON));
                trip.date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRIP_DATE));
                trips.add(trip);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return trips;
    }

    // Get trips for a specific user
    public List<Trip> getUserTrips(int userId) {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRIPS, null,
                COL_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COL_TRIP_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Trip trip = new Trip();
                trip.tripId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID));
                trip.userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID_FK));
                trip.userName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME_FK));
                trip.from = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_FROM));
                trip.to = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_TO));
                trip.mode = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_MODE));
                trip.distance = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_DISTANCE));
                trip.carbonSaved = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_CARBON));
                trip.date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRIP_DATE));
                trips.add(trip);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return trips;
    }

    // Get recent trips (last 5) for a user
    public List<Trip> getRecentTrips(int userId, int limit) {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRIPS, null,
                COL_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COL_TRIP_DATE + " DESC", String.valueOf(limit));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Trip trip = new Trip();
                trip.tripId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRIP_ID));
                trip.userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID_FK));
                trip.userName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME_FK));
                trip.from = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_FROM));
                trip.to = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_TO));
                trip.mode = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIP_MODE));
                trip.distance = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_DISTANCE));
                trip.carbonSaved = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRIP_CARBON));
                trip.date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRIP_DATE));
                trips.add(trip);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return trips;
    }

    // Save trip with user stats update
    public boolean saveTrip(int userId, String userName, String from, String to,
                            String mode, double distance, double carbonSaved) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Insert trip
            ContentValues values = new ContentValues();
            values.put(COL_USER_ID_FK, userId);
            values.put(COL_USER_NAME_FK, userName);
            values.put(COL_TRIP_FROM, from);
            values.put(COL_TRIP_TO, to);
            values.put(COL_TRIP_MODE, mode);
            values.put(COL_TRIP_DISTANCE, distance);
            values.put(COL_TRIP_CARBON, carbonSaved);
            values.put(COL_TRIP_DATE, System.currentTimeMillis());

            long result = db.insert(TABLE_TRIPS, null, values);

            if (result != -1) {
                // Update user stats
                updateUserStats(userId, carbonSaved, 1);
                db.setTransactionSuccessful();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // Update user statistics
    public void updateUserStats(int userId, double carbonSaved, int tripsAdded) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_TOTAL_CARBON, COL_TOTAL_TRIPS},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            double currentCarbon = cursor.getDouble(0);
            int currentTrips = cursor.getInt(1);
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COL_TOTAL_CARBON, currentCarbon + carbonSaved);
            values.put(COL_TOTAL_TRIPS, currentTrips + tripsAdded);

            db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        }
        if (cursor != null) cursor.close();
    }

    // Get total carbon saved by a user
    public double getTotalCarbonSaved(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_TOTAL_CARBON},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            double carbon = cursor.getDouble(0);
            cursor.close();
            return carbon;
        }
        if (cursor != null) cursor.close();
        return 0;
    }

    // Get total trips count for a user
    public int getTotalTripsCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_TOTAL_TRIPS},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int trips = cursor.getInt(0);
            cursor.close();
            return trips;
        }
        if (cursor != null) cursor.close();
        return 0;
    }

    // Delete user account (admin only)
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Delete user's trips first
            db.delete(TABLE_TRIPS, COL_USER_ID_FK + "=?", new String[]{String.valueOf(userId)});
            // Delete user
            int result = db.delete(TABLE_USERS, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
            db.setTransactionSuccessful();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // Get admin user
    public User getAdminUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_ROLE + "=?", new String[]{"admin"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            user.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
            user.email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
            user.role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
            user.totalCarbon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_CARBON));
            user.totalTrips = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_TRIPS));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // Get total number of users
    public int getTotalUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"COUNT(*)"},
                COL_USER_ROLE + "=?", new String[]{"user"}, null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        if (cursor != null) cursor.close();
        return count;
    }

    // Get total carbon saved by all users
    public double getTotalCarbonAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_TOTAL_CARBON + ") FROM " + TABLE_USERS +
                " WHERE " + COL_USER_ROLE + "=?", new String[]{"user"});

        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        if (cursor != null) cursor.close();
        return total;
    }
}
