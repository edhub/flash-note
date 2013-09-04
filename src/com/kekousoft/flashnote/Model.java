
package com.kekousoft.flashnote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Model {

    private static Model sModel;

    private ArrayList<Note> sNotes;

    private ArrayList<DataChangeObserver> sObservers = new ArrayList<Model.DataChangeObserver>();

    private Model(ArrayList<Note> notes) {
        sNotes = notes;
    }

    private static ArrayList<Note> createModel(Context context) {
        ArrayList<Note> model = new ArrayList<Note>();
        SQLiteDatabase db = (new DbHelper(context)).getReadableDatabase();
        Cursor c = db.query(Note.TABLE_NAME, null, null, null, null, null,
                Note.COL_DUEDATE + " desc");
        int col_id = c.getColumnIndex(Note.COL_ID);
        int col_desc = c.getColumnIndex(Note.COL_DESC);
        int col_dueDate = c.getColumnIndex(Note.COL_DUEDATE);
        int col_voice = c.getColumnIndex(Note.COL_VOICE);
        int col_prio = c.getColumnIndex(Note.COL_PRIO);
        while (c.moveToNext()) {
            model.add(new Note(c.getInt(col_id), c.getString(col_desc), c.getLong(col_dueDate),
                    c.getString(col_voice), c.getInt(col_prio)));
        }
        c.close();
        db.close();
        return model;
    }

    public static Model getModel(Context context, boolean createIfNotExist) {
        if (sModel == null && createIfNotExist) {
            sModel = new Model(createModel(context));
        }
        return sModel;
    }

    public static void destoryModel() {
        sModel = null;
    }

    public ArrayList<Note> getNotes() {
        return sNotes;
    }

    public void addNote(Note note) {
        int count = sNotes.size();
        int index = 0;
        for (int i = 0; i < count; i++) {
            if (sNotes.get(i).dueDate < note.dueDate) {
                index = i;
                break;
            }
        }
        sNotes.add(index, note);
        for (DataChangeObserver observer : sObservers) {
            observer.notifyModelChanged();
        }
    }

    public void registerObserver(DataChangeObserver observer) {
        sObservers.add(observer);
    }

    public static interface DataChangeObserver {
        public void notifyModelChanged();
    }
}
