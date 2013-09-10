
package com.kekousoft.flashnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Controller {

    public static final int MODEL_ALL = 1;

    public static final int MODEL_DONE = 2;

    public static final int MODEL_ONGOING = 3;

    private static Controller sController;

    private static Comparator<Note> sCompDueDateDesc = new Comparator<Note>() {
        @Override
        public int compare(Note lhs, Note rhs) {
            return (int)(rhs.dueDate - lhs.dueDate);
        }
    };

    private static Comparator<Note> sCompDueDateAsc = new Comparator<Note>() {
        @Override
        public int compare(Note lhs, Note rhs) {
            if (lhs.dueDate == 0) {
                return Integer.MAX_VALUE;
            } else if (rhs.dueDate == 0) {
                return Integer.MIN_VALUE;
            }
            return (int)(lhs.dueDate - rhs.dueDate);
        }
    };

    private static Comparator<Note> sCompFinishDesc = new Comparator<Note>() {
        @Override
        public int compare(Note lhs, Note rhs) {
            if (lhs.dueDate == 0) {
                return Integer.MAX_VALUE;
            } else if (rhs.dueDate == 0) {
                return Integer.MIN_VALUE;
            }
            return (int)(rhs.finishedOn - lhs.finishedOn);
        }
    };

    private ArrayList<Note> mAllNotes;

    private ArrayList<Note> mDoneNotes;

    private ArrayList<Note> mOngoingNotes;

    private ArrayList<DataChangeObserver> mObservers = new ArrayList<Controller.DataChangeObserver>();

    private Controller(ArrayList<Note> all) {
        mAllNotes = all;

        mDoneNotes = new ArrayList<Note>();
        for (Note note : mAllNotes) {
            if (note.finishedOn > 0) {
                mDoneNotes.add(note);
            }
        }
        Collections.sort(mDoneNotes, sCompFinishDesc);

        mOngoingNotes = new ArrayList<Note>();
        for (Note note : mAllNotes) {
            if (note.finishedOn == 0) {
                mOngoingNotes.add(0, note);
            }
        }
        Collections.sort(mOngoingNotes, sCompDueDateAsc);

    }

    private static Controller createController(Context context) {
        SQLiteDatabase db = (new DbHelper(context)).getReadableDatabase();
        Cursor c = db.query(Note.TABLE_NAME, null, null, null, null, null,
                Note.COL_DUEDATE + " desc");
        int col_id = c.getColumnIndex(Note.COL_ID);
        int col_desc = c.getColumnIndex(Note.COL_DESC);
        int col_dueDate = c.getColumnIndex(Note.COL_DUEDATE);
        int col_voice = c.getColumnIndex(Note.COL_VOICE);
        int col_color = c.getColumnIndex(Note.COL_COLOR);
        int col_finishedOn = c.getColumnIndex(Note.COL_FINISHED_ON);
        ArrayList<Note> notes = new ArrayList<Note>();

        while (c.moveToNext()) {
            notes.add(new Note(c.getInt(col_id), c.getString(col_desc), c.getLong(col_dueDate),
                    c.getString(col_voice), c.getInt(col_color), c.getLong(col_finishedOn)));
        }
        c.close();
        db.close();

        return new Controller(notes);
    }

    public static Controller getInstance(Context context, boolean createIfNotExist) {
        if (sController == null && createIfNotExist) {
            sController = createController(context);
            sController.registerObserver(new AlarmMaker(context.getApplicationContext()));
        }
        return sController;
    }

    public static void releaseModel() {
        sController = null;
    }

    public ArrayList<Note> getModel(int which) {
        switch (which) {
            case MODEL_ALL:
                return mAllNotes;
            case MODEL_DONE:
                return mDoneNotes;
            default:
                return mOngoingNotes;
        }
    }

    public void deleteNote(Context context, Note note) {
        mAllNotes.remove(note);
        mDoneNotes.remove(note);
        mOngoingNotes.remove(note);
        if (note.voiceRecord.length() > 0) {
            context.deleteFile(note.voiceRecord);
        }
        SQLiteDatabase db = (new DbHelper(context)).getWritableDatabase();
        db.delete(Note.TABLE_NAME, Note.COL_ID + "=" + note.id, null);
        db.close();
        notifyModelChanged();
    }

    public static Note getNoteById(Context context, long id) {
        Note note = null;
        if (sController != null) {
            note = sController.getNote(id);
        }

        if (note == null) {
            SQLiteDatabase db = (new DbHelper(context)).getReadableDatabase();
            Cursor c = db.query(Note.TABLE_NAME, null, Note.COL_ID + "=" + id, null, null, null,
                    Note.COL_DUEDATE + " desc");
            if (c.moveToFirst()) {
                int col_id = c.getColumnIndex(Note.COL_ID);
                int col_desc = c.getColumnIndex(Note.COL_DESC);
                int col_dueDate = c.getColumnIndex(Note.COL_DUEDATE);
                int col_voice = c.getColumnIndex(Note.COL_VOICE);
                int col_color = c.getColumnIndex(Note.COL_COLOR);
                int col_finishedOn = c.getColumnIndex(Note.COL_FINISHED_ON);
                note = new Note(c.getInt(col_id), c.getString(col_desc),
                        c.getLong(col_dueDate),
                        c.getString(col_voice), c.getInt(col_color), c.getLong(col_finishedOn));
            }
        }
        return note;
    }

    public Note getNote(long id) {
        for (Note note : mAllNotes) {
            if (note.id == id) {
                return note;
            }
        }
        return null;
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

        if (sController != null) {
            sController.newNote(note);
        }
    }

    private static void updateNoteDb(Context context, Note note) {
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

    public static void updateNote(Context context, Note note) {
        updateNoteDb(context, note);
        if (sController != null) {
            sController.onNoteUpdated(note);
        }
    }

    /**
     * Sort and notify data observers.
     *
     * @param note
     */
    public void onNoteUpdated(Note note) {
        Collections.sort(mAllNotes, sCompDueDateDesc);
        if (note.finishedOn > 0) {
            Collections.sort(mDoneNotes, sCompFinishDesc);
        } else {
            Collections.sort(mOngoingNotes, sCompDueDateAsc);
        }
        notifyModelChanged();
    }

    /**
     * Mark a note as finished, and notify data observers.
     *
     * @param context
     * @param note
     */
    public void finishNote(Context context, Note note) {
        note.finishedOn = System.currentTimeMillis();
        updateNoteDb(context, note);
        mDoneNotes.add(0, note);
        mOngoingNotes.remove(note);
        notifyModelChanged();
    }

    public void reOpenNote(Context context, Note note) {
        note.finishedOn = 0;
        updateNoteDb(context, note);
        mDoneNotes.remove(note);
        mOngoingNotes.add(note);
        Collections.sort(mOngoingNotes, sCompDueDateAsc);
        notifyModelChanged();
    }

    private void newNote(Note note) {
        mAllNotes.add(note);
        Collections.sort(mAllNotes, sCompDueDateDesc);
        mOngoingNotes.add(note);
        Collections.sort(mOngoingNotes, sCompDueDateAsc);
        notifyModelChanged();
    }

    public void notifyModelChanged() {
        for (DataChangeObserver observer : mObservers) {
            observer.notifyModelChanged();
        }
    }

    public void registerObserver(DataChangeObserver observer) {
        mObservers.add(observer);
    }

    public void unRegisterObserver(DataChangeObserver observer) {
        mObservers.remove(observer);
    }

    public static interface DataChangeObserver {
        public void notifyModelChanged();
    }
}
