
package com.kekousoft.flashnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Model {

    public static final int ALL = 1;

    public static final int DONE = 2;

    public static final int ONGOING = 3;

    private static Model sModel;

    private ArrayList<Note> sAllNotes;

    private ArrayList<Note> sDoneNotes;

    private ArrayList<Note> sOngoingNotes;

    private ArrayList<DataChangeObserver> sObservers = new ArrayList<Model.DataChangeObserver>();

    private Model(ArrayList<Note> all, ArrayList<Note> done, ArrayList<Note> ongoing) {
        sAllNotes = all;
        sDoneNotes = done;
        sOngoingNotes = ongoing;
    }

    private static Model createModel(Context context) {
        SQLiteDatabase db = (new DbHelper(context)).getReadableDatabase();
        Cursor c = db.query(Note.TABLE_NAME, null, null, null, null, null,
                Note.COL_DUEDATE + " desc");
        int col_id = c.getColumnIndex(Note.COL_ID);
        int col_desc = c.getColumnIndex(Note.COL_DESC);
        int col_dueDate = c.getColumnIndex(Note.COL_DUEDATE);
        int col_voice = c.getColumnIndex(Note.COL_VOICE);
        int col_prio = c.getColumnIndex(Note.COL_COLOR);
        int col_finishedOn = c.getColumnIndex(Note.COL_FINISHED_ON);
        ArrayList<Note> all = new ArrayList<Note>();
        ArrayList<Note> done = new ArrayList<Note>();
        ArrayList<Note> ongoing = new ArrayList<Note>();

        while (c.moveToNext()) {
            Note note = new Note(c.getInt(col_id), c.getString(col_desc), c.getLong(col_dueDate),
                    c.getString(col_voice), c.getInt(col_prio), c.getLong(col_finishedOn));
            all.add(note);
            if (note.finishedOn > 0) {
                done.add(note);
            } else {
                if (note.dueDate == 0) {
                    ongoing.add(note);
                } else {
                    ongoing.add(0, note);
                }
            }
        }
        c.close();
        db.close();
        return new Model(all, done, ongoing);
    }

    public static Model getModel(Context context, boolean createIfNotExist) {
        if (sModel == null && createIfNotExist) {
            sModel = createModel(context);
        }
        return sModel;
    }

    public static void releaseModel() {
        sModel = null;
    }

    public ArrayList<Note> getNotes(int which) {
        switch (which) {
            case ALL:
                return sAllNotes;
            case DONE:
                if (sDoneNotes == null) {
                    sDoneNotes = new ArrayList<Note>();

                }
                return sDoneNotes;
            default:
                return sOngoingNotes;
        }
    }

    public void deleteNote(Context context, Note note) {
        sAllNotes.remove(note);
        sDoneNotes.remove(note);
        sOngoingNotes.remove(note);
        if (note.voiceRecord.length() > 0) {
            context.deleteFile(note.voiceRecord);
        }
        SQLiteDatabase db = (new DbHelper(context)).getWritableDatabase();
        db.delete(Note.TABLE_NAME, Note.COL_ID + "=" + note.id, null);
        db.close();
        notifyModelChanged();
    }

    public static void insertNote(Context context, Note note) {
        SQLiteDatabase db = (new DbHelper(context)).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Note.COL_DESC, note.description);
        cv.put(Note.COL_DUEDATE, note.dueDate);
        cv.put(Note.COL_COLOR, note.color);
        cv.put(Note.COL_VOICE, note.voiceRecord);
        cv.put(Note.COL_FINISHED_ON, note.finishedOn);
        note.id = db.insert(Note.TABLE_NAME, null, cv);
        db.close();

        if (sModel != null) {
            sModel.addNote(note);
        }
    }

    public void finishNote(Context context, Note note) {
        note.finishedOn = System.currentTimeMillis();
        updateNote(context, note);
        sDoneNotes.add(0, note);
        sOngoingNotes.remove(note);
        notifyModelChanged();
    }

    public void reOpenNote(Context context, Note note) {
        note.finishedOn = 0;
        updateNote(context, note);
        sDoneNotes.remove(note);
        insertOngoing(note);
        notifyModelChanged();
    }

    private void updateNote(Context context, Note note) {
        SQLiteDatabase db = (new DbHelper(context)).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Note.COL_DESC, note.description);
        cv.put(Note.COL_DUEDATE, note.dueDate);
        cv.put(Note.COL_COLOR, note.color);
        cv.put(Note.COL_VOICE, note.voiceRecord);
        cv.put(Note.COL_FINISHED_ON, note.finishedOn);
        db.update(Note.TABLE_NAME, cv, Note.COL_ID + "=" + note.id, null);
        db.close();
    }

    private void addNote(Note note) {
        sAllNotes.add(0, note);
        insertOngoing(note);
        notifyModelChanged();
    }

    private void insertOngoing(Note note) {
        if (note.dueDate == 0 || sOngoingNotes.size() == 0) {
            sOngoingNotes.add(note);
            return;
        }
        int count = sOngoingNotes.size();
        for (int i = 0; i < count; i++) {
            if (sOngoingNotes.get(i).dueDate > note.dueDate
                    || sOngoingNotes.get(i).dueDate == 0
                    || i == count - 1) {
                sOngoingNotes.add(i, note);
                break;
            }
        }
    }

    public void notifyModelChanged() {
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
