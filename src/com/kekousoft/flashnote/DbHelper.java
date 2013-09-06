
package com.kekousoft.flashnote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "flashnote";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + Note.TABLE_NAME + " ("
                + Note.COL_ID + " INTEGER PRIMARY KEY, "
                + Note.COL_COLOR + " INTEGER, "
                + Note.COL_DESC + " TEXT, "
                + Note.COL_VOICE + " TEXT, "
                + Note.COL_DUEDATE + " INTEGER,"
                + Note.COL_FINISHED_ON + " INTEGER"
                + ")";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
