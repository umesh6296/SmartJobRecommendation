package com.example.smartjobrecommendation.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "JobPortal.db";
    private static final int DATABASE_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE chats(" +
                "chatId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "senderId INTEGER," +
                "receiverId INTEGER," +
                "message TEXT," +
                "timestamp LONG," +
                "jobId INTEGER," +
                "isRead INTEGER DEFAULT 0)");

        // USER TABLE
        db.execSQL("CREATE TABLE users(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "role TEXT," +
                "name TEXT," +
                "companyName TEXT," +
                "aboutCompany TEXT," +
                "phone TEXT," +
                "email TEXT UNIQUE," +
                "password TEXT," +
                "qualification TEXT," +
                "qualification_details TEXT," +
                "skills TEXT," +
                "experience TEXT," +
                "photo TEXT," +
                "resume TEXT," +
                "companyType TEXT)");

        // JOB TABLE - with mandatoryDegree column
        db.execSQL("CREATE TABLE jobs(" +
                "jobId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hrId INTEGER," +
                "jobTitle TEXT," +
                "jobDesc TEXT," +
                "requiredSkills TEXT," +
                "requiredExp TEXT," +
                "salary TEXT," +
                "mandatoryDegree TEXT)");

        // APPLICATION TABLE
        db.execSQL("CREATE TABLE applications(" +
                "appId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "jobId INTEGER," +
                "userId INTEGER," +
                "status TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE chats(" +
                    "chatId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "senderId INTEGER," +
                    "receiverId INTEGER," +
                    "message TEXT," +
                    "timestamp LONG," +
                    "jobId INTEGER," +
                    "isRead INTEGER DEFAULT 0)");
        } else {
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS jobs");
            db.execSQL("DROP TABLE IF EXISTS applications");
            db.execSQL("DROP TABLE IF EXISTS chats");
            onCreate(db);
        }
    }

    public boolean sendMessage(int senderId, int receiverId, String message, int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("senderId", senderId);
        cv.put("receiverId", receiverId);
        cv.put("message", message);
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("jobId", jobId);
        cv.put("isRead", 0);

        long result = db.insert("chats", null, cv);
        db.close();
        return result != -1;
    }
    public Cursor getChatHistory(int user1Id, int user2Id, int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM chats WHERE " +
                "(senderId=? AND receiverId=? AND jobId=?) OR " +
                "(senderId=? AND receiverId=? AND jobId=?) " +
                "ORDER BY timestamp ASC";

        String[] args = {String.valueOf(user1Id), String.valueOf(user2Id), String.valueOf(jobId),
                String.valueOf(user2Id), String.valueOf(user1Id), String.valueOf(jobId)};

        return db.rawQuery(query, args);
    }
    public void markMessagesAsRead(int userId, int otherUserId, int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isRead", 1);

        db.update("chats", cv,
                "receiverId=? AND senderId=? AND jobId=? AND isRead=0",
                new String[]{String.valueOf(userId), String.valueOf(otherUserId), String.valueOf(jobId)});
        db.close();
    }


    public int getHrIdForJob(int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT hrId FROM jobs WHERE jobId=?",
                new String[]{String.valueOf(jobId)});

        int hrId = -1;
        if (cursor.moveToFirst()) {
            hrId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return hrId;
    }
    public String getUserNameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users WHERE id=?",
                new String[]{String.valueOf(userId)});

        String name = "User";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }
    // ---------------- REGISTER USER (with qualificationDetails) ----------------
    public boolean registerUser(String role, String name, String companyName,
                                String aboutCompany, String phone,
                                String email, String password,
                                String qualification,
                                String qualificationDetails,
                                String skills,
                                String experience,
                                String photo,
                                String resume,
                                String companyType) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("role", role);
        cv.put("name", name);
        cv.put("companyName", companyName);
        cv.put("aboutCompany", aboutCompany);
        cv.put("phone", phone);
        cv.put("email", email);
        cv.put("password", password);
        cv.put("qualification", qualification);
        cv.put("qualification_details", qualificationDetails);
        cv.put("skills", skills);
        cv.put("experience", experience);
        cv.put("photo", photo);
        cv.put("resume", resume);
        cv.put("companyType", companyType);

        long result = -1;
        try {
            result = db.insert("users", null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return result != -1;
    }

    // ---------------- REGISTER USER (without qualificationDetails - for HR) ----------------
    public boolean registerUser(String role, String name, String companyName,
                                String aboutCompany, String phone,
                                String email, String password,
                                String qualification,
                                String skills,
                                String experience,
                                String photo,
                                String resume,
                                String companyType) {

        return registerUser(role, name, companyName, aboutCompany, phone, email, password,
                qualification, "", skills, experience, photo, resume, companyType);
    }

    // ---------------- LOGIN ----------------
    public Cursor loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE email=? AND password=?",
                new String[]{email, password});
    }

    // ---------------- POST JOB (with mandatoryDegree) ----------------
    public boolean postJob(int hrId, String title, String desc,
                           String skills, String exp, String salary,
                           String mandatoryDegree) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("hrId", hrId);
        cv.put("jobTitle", title);
        cv.put("jobDesc", desc);
        cv.put("requiredSkills", skills);
        cv.put("requiredExp", exp);
        cv.put("salary", salary);
        cv.put("mandatoryDegree", mandatoryDegree);

        long result = db.insert("jobs", null, cv);
        db.close();
        return result != -1;
    }

    // ---------------- APPLY JOB ----------------
    public boolean applyJob(int jobId, int userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Check if already applied
        Cursor cursor = db.rawQuery(
                "SELECT * FROM applications WHERE jobId=? AND userId=?",
                new String[]{String.valueOf(jobId), String.valueOf(userId)}
        );

        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false; // Already Applied
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("jobId", jobId);
        cv.put("userId", userId);
        cv.put("status", "Applied");

        long result = db.insert("applications", null, cv);
        db.close();
        return result != -1;
    }

    // ---------------- UPDATE STATUS ----------------
    public void updateStatus(int jobId, int userId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);

        db.update("applications",
                cv,
                "jobId=? AND userId=?",
                new String[]{String.valueOf(jobId), String.valueOf(userId)});
        db.close();
    }

    // GET USER SKILLS
    public String getUserSkills(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT skills FROM users WHERE id=?",
                new String[]{String.valueOf(userId)});

        String skills = "";
        if (cursor.moveToFirst()) {
            skills = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return skills;
    }

    // GET JOB REQUIRED SKILLS
    public String getJobSkills(int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT requiredSkills FROM jobs WHERE jobId=?",
                new String[]{String.valueOf(jobId)});

        String skills = "";
        if (cursor.moveToFirst()) {
            skills = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return skills;
    }

    // GET APPLICATION STATUS


    // UPDATE USER
    public boolean updateUser(int userId,
                              String name,
                              String phone,
                              String email,
                              String password,
                              String qualification,
                              String qualificationDetails,
                              String skills,
                              String experience,
                              String photo,
                              String resume) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);
        values.put("email", email);

        // Only update password if it's provided and not empty
        if (password != null && !password.isEmpty()) {
            values.put("password", password);
        }

        values.put("qualification", qualification);
        values.put("qualification_details", qualificationDetails);
        values.put("skills", skills);
        values.put("experience", experience);
        values.put("photo", photo);
        values.put("resume", resume);

        int result = db.update(
                "users",
                values,
                "id=?",
                new String[]{String.valueOf(userId)}
        );

        db.close();
        return result > 0;
    }
}