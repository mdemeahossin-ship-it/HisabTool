package com.maruf.hisabtool.Database_Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BakiKhata.db";

    // Database Version
    private static final int DATABASE_VERSION = 11;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // টেবিল তৈরি
        sqLiteDatabase.execSQL("CREATE TABLE customers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, due TEXT)");
        sqLiteDatabase.execSQL("CREATE TABLE history_table (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, type TEXT, amount TEXT)");
        sqLiteDatabase.execSQL("CREATE TABLE history_table_due (id INTEGER PRIMARY KEY AUTOINCREMENT, date_1 TEXT, type_1 TEXT, due TEXT)");

        // ফাস্ট লোডিংয়ের জন্য ইনডেক্স তৈরি
        sqLiteDatabase.execSQL("CREATE INDEX idx_cust_id ON customers(id)");
        sqLiteDatabase.execSQL("CREATE INDEX idx_hist_id ON history_table(id)");
        sqLiteDatabase.execSQL("CREATE INDEX idx_due_id ON history_table_due(id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS customers");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS history_table");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS history_table_due");
        onCreate(sqLiteDatabase);

    }
    ///  customers INSERT / get_All_date  ///
    ///  customers INSERT / get_All_date  ///
    public boolean Insert_Data(String name, String phone) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM customers WHERE phone=?", new String[]{phone});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        if (cursor != null) cursor.close();

        ContentValues content = new ContentValues();
        content.put("name", name);
        content.put("phone", phone);
        // 🎯 ফিক্স ২: নতুন কাস্টমার খোলার সময় তার বাকি শুরুতে '৳ 0' সেট হবে যাতে হোম পেজ নাল (Null) না পায়
        content.put("due", "৳ 0");

        return database.insert("customers", null, content) != -1;
    }

    public Cursor get_All_date(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("SELECT * FROM customers ORDER BY id DESC", null);
        return cursor;
    }

    /// delete code ///
    public void deleteCustomer(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("customers", "phone = ?", new String[]{phone});
        db.close();
    }

    /// search_view code ///
    public Cursor search_view(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM customers WHERE name LIKE ?", new String[]{"%" + name + "%"});
        return cursor;
    }

    /// updateCustomerDue ///
    public void updateCustomerDue(String phone, double finalDue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("due", "৳ " + (int)finalDue);

        db.update("customers", values, "phone = ?", new String[]{phone});
        db.close();
    }

    /// NEW HISTORY INSERT / GET_date ///
    public boolean insertHistory(String date, String type, String amount){
        SQLiteDatabase database_1 = this.getWritableDatabase();
        Cursor cursor = database_1.rawQuery("SELECT * FROM history_table WHERE date=? AND type=? AND amount=?", new String[]{date, type, amount});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        if (cursor != null) cursor.close();

        ContentValues content_1 = new ContentValues();
        content_1.put("date", date);
        content_1.put("type", type);
        content_1.put("amount", amount);

        long result = database_1.insert("history_table", null, content_1);
        return result != -1;
    }

    public Cursor getHistory(){
        SQLiteDatabase db_1 = this.getReadableDatabase();
        Cursor cursor_1 = db_1.rawQuery("SELECT * FROM history_table ORDER BY id DESC", null);
        return cursor_1;
    }

    ///  insertHistory_due INSERT / getHistory_due  ///
    public boolean insertHistory_due(String date_1, String type_1, String due){
        SQLiteDatabase database_2 = this.getWritableDatabase();
        Cursor cursor = database_2.rawQuery("SELECT * FROM history_table_due WHERE date_1=? AND type_1=? AND due=?", new String[]{date_1, type_1, due});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        if (cursor != null) cursor.close();

        ContentValues content_2 = new ContentValues();
        content_2.put("date_1", date_1);
        content_2.put("type_1", type_1);
        content_2.put("due", due);

        long result = database_2.insert("history_table_due", null, content_2);
        return result != -1;
    }

    public Cursor getHistory_due(){
        SQLiteDatabase db_2 = this.getReadableDatabase();
        Cursor cursor_2 = db_2.rawQuery("SELECT * FROM history_table_due ORDER BY id DESC", null);
        return cursor_2;
    }

    public boolean isCustomerExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.rawQuery("SELECT * FROM customers WHERE phone = ?", new String[]{phone});
            if (cursor != null && cursor.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }


///================= clearAllData =====================///
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // আপনার লোকাল SQLite ডাটাবেজের সব টেবিল থেকে ডাটা ডিলিট করা হচ্ছে
            db.execSQL("DELETE FROM customers");
            db.execSQL("DELETE FROM history_table");
            db.execSQL("DELETE FROM history_table_due");

            // অটো-ইনক্রিমেন্ট বা প্রাইমারি কি (ID) ১ থেকে শুরু করার জন্য সিকোয়েন্স রিসেট ভাই
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='customers'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='history_table'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='history_table_due'");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // ডাটাবেজ কানেকশন ওপেন থাকলে তা সেফটি হিসেবে বন্ধ করা হচ্ছে না, তবে প্রয়োজন হলে ক্লোজ করতে পারেন
        }
    }
}