package com.dqk.myapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "DuLieu.db";
    private static final int DB_VERSION = 1;
    //private static final int DB_VERSION = 2 // Nếu cần nâng cấp
    private static DatabaseHelper instance;
    private final Context context;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        copyDatabaseIfNeeded();
    }

    private void copyDatabaseIfNeeded() {
        // Đường dẫn DB trên thiết bị
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        File dbFile = new File(dbPath);

        // Nếu file chưa tồn tại thì mới copy
        if (!dbFile.exists()) {
            // Tạo thư mục databases nếu chưa có
            dbFile.getParentFile().mkdirs();
            try {
                InputStream input = context.getAssets().open(DB_NAME);
                OutputStream output = new FileOutputStream(dbPath);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                output.flush();
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Không cần làm gì — bảng đã có sẵn trong file .db
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Khi cần nâng cấp: xóa file cũ, copy file mới từ assets
        context.getDatabasePath(DB_NAME).delete();
        copyDatabaseIfNeeded();
    }

    // Mở kết nối để dùng
    public SQLiteDatabase openDatabase() {
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

}