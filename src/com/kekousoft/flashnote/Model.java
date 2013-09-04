
package com.kekousoft.flashnote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Model {

    public static final int ALL = 1;

    public static final int PAST = 2;

    public static final int UPCOMING = 3;

    public static final int NO_DUEDATE = 4;

    private static Model sModel;

    private ArrayList<Note> sAllNotes;

    private ArrayList<Note> sPastNotes;

    private ArrayList<Note> sUpcomingNotes;

    private ArrayList<Note> sNoDuedateNotes;

    private ArrayList<DataChangeObserver> sObservers = new ArrayList<Model.DataChangeObserver>();

    private Model(ArrayList<Note> all, ArrayList<Note> past, ArrayList<Note> upcoming,
            ArrayList<Note> no_duedate) {
        sAllNotes = all;
        sPastNotes = past;
        sUpcomingNotes = upcoming;
        sNoDuedateNotes = no_duedate;
    }

    private static Model createModel(Context context) {
        SQLiteDatabase db = (new DbHelper(context)).getReadableDatabase();
        Cursor c = db.query(Note.TABLE_NAME, null, null, null, null, null,
                Note.COL_DUEDATE + " desc");
        int col_id = c.getColumnIndex(Note.COL_ID);
        int col_desc = c.getColumnIndex(Note.COL_DESC);
        int col_dueDate = c.getColumnIndex(Note.COL_DUEDATE);
        int col_voice = c.getColumnIndex(Note.COL_VOICE);
        int col_prio = c.getColumnIndex(Note.COL_PRIO);
        ArrayList<Note> mdl_all = new ArrayList<Note>();
        ArrayList<Note> mdl_past = new ArrayList<Note>();
        ArrayList<Note> mdl_upcoming = new ArrayList<Note>();
        ArrayList<Note> mdl_noduedate = new ArrayList<Note>();

        long now = System.currentTimeMillis();
        while (c.moveToNext()) {
            Note note = new Note(c.getInt(col_id), c.getString(col_desc), c.getLong(col_dueDate),
                    c.getString(col_voice), c.getInt(col_prio));
            mdl_all.add(note);
            if (note.dueDate == 0) {
                mdl_noduedate.add(note);
            } else if (note.dueDate < now) {
                mdl_past.add(0, note);
            } else {
                mdl_upcoming.add(0, note);
            }
        }
        c.close();
        db.close();
        return new Model(mdl_all, mdl_past, mdl_upcoming, mdl_noduedate);
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
            case PAST:
                return sPastNotes;
            case NO_DUEDATE:
                return sNoDuedateNotes;
            default:
                return sUpcomingNotes;
        }
    }

    public void addNote(Note note) {
        if (note.dueDate == 0) {
            sNoDuedateNotes.add(0, note);
        } else {
            insertUpcoming(note);
        }

        int count = sAllNotes.size();
        for (int i = 0; i < count; i++) {
            if (sAllNotes.get(i).dueDate < note.dueDate) {
                sAllNotes.add(i, note);
                break;
            }
        }

        for (DataChangeObserver observer : sObservers) {
            observer.notifyModelChanged();
        }
    }

    private void insertUpcoming(Note note) {
        int count = sUpcomingNotes.size();
        for (int i = 0; i < count; i++) {
            if (sUpcomingNotes.get(i).dueDate > note.dueDate) {
                sUpcomingNotes.add(i + 1, note);
                break;
            }
        }
    }

    public void registerObserver(DataChangeObserver observer) {
        sObservers.add(observer);
    }

    public static interface DataChangeObserver {
        public void notifyModelChanged();
    }
}
