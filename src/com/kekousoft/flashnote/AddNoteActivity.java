
package com.kekousoft.flashnote;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AddNoteActivity extends Activity {

    private int sCurrentPrio = Note.PRIO_NORMAL;

    private long sDueDate;

    private String sVoiceFile = "";

    private int sBgSelected;

    private int sBgNotSeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        Resources res = getResources();
        sBgSelected = res.getColor(R.color.prio_selected);
        sBgNotSeleted = res.getColor(R.color.prio_not_selected);

        final TextView tv_date = (TextView)findViewById(R.id.tv_date);
        SeekBar sb_date = (SeekBar)findViewById(R.id.sb_date);
        sb_date.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_date.setText(getTime(progress));
            }
        });
    }

    public void saveNote(View v) {
        SQLiteDatabase db = (new DbHelper(this).getWritableDatabase());
        Note note = new Note(0, ((EditText)findViewById(R.id.et_desc)).getText().toString(),
                sDueDate, sVoiceFile, sCurrentPrio);
        ContentValues cv = new ContentValues();

        cv.put(Note.COL_DESC, note.description);
        cv.put(Note.COL_DUEDATE, note.dueDate);
        cv.put(Note.COL_PRIO, note.priority);
        cv.put(Note.COL_VOICE, note.voiceRecord);
        note.id = db.insert(Note.TABLE_NAME, null, cv);
        db.close();

        Model model = Model.getModel(this, false);
        if (model != null) {
            model.addNote(note);
        }

        finish();
    }

    public void cancelEdit(View v) {
        // destroy recorded voice, etc
        finish();
    }

    private String getTime(int progress) {
        String display = "";
        if (progress > 0 && progress <= 25) {
            int num = (int)(progress * 1.0f / 25 * 60);
            sDueDate = System.currentTimeMillis() + num * 60 * 1000;
            display = num + " mins";
        } else if (progress > 25 && progress <= 50) {
            int num = (int)((progress - 24) * 1.0f / 25 * 24);
            sDueDate = System.currentTimeMillis() + num * 60 * 60 * 1000;
            display = num + " hours";
        } else if (progress > 50 && progress <= 75) {
            int num = (int)((progress - 50) * 1.0f / 25 * 30);
            sDueDate = System.currentTimeMillis() + num * 24 * 60 * 60 * 1000;
            display = num + " days";
        } else if (progress > 75) {
            int num = (int)((progress - 74) * 1.0f / 2);
            sDueDate = System.currentTimeMillis() + num * 30 * 24 * 60 * 60 * 1000;
            display = num + " month";
        }

        return display;

    }

    public void onPrioClicked(View v) {

        int prio = getPrio(v.getId());
        if (prio != sCurrentPrio) {
            unSelectPrio(sCurrentPrio);
            sCurrentPrio = prio;
            v.setBackgroundColor(sBgSelected);
        }
    }

    private int getPrio(int id) {
        switch (id) {
            case R.id.tv_prio_low:
                return Note.PRIO_LOW;
            case R.id.tv_prio_high:
                return Note.PRIO_HIGH;
            default:
                return Note.PRIO_NORMAL;
        }
    }

    private void unSelectPrio(int prio) {
        int id;
        switch (prio) {
            case Note.PRIO_LOW:
                id = R.id.tv_prio_low;
                break;
            case Note.PRIO_HIGH:
                id = R.id.tv_prio_high;
                break;
            default:
                id = R.id.tv_prio_normal;
                break;
        }
        View v = findViewById(id);
        v.setBackgroundColor(sBgNotSeleted);
    }
}
