
package com.kekousoft.flashnote;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AddNoteActivity extends Activity {

    private int sCurrentPrio = Note.PRIO_NORMAL;

    private long sDueDate;

    private boolean sDiscardNote = true;

    private boolean sIsRecording = false;

    private String sVoiceFile = "";

    private VoiceHelper sVoiceHelper;

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
        sb_date.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        sVoiceHelper = new VoiceHelper(this);
        final Button btn_play = (Button)findViewById(R.id.btn_play);
        final Button btn_record = (Button)findViewById(R.id.btn_record);
        btn_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!sIsRecording && event.getAction() == MotionEvent.ACTION_DOWN) {
                    sIsRecording = true;
                    if (sVoiceFile.length() > 0) {
                        deleteFile(sVoiceFile);
                    }
                    sVoiceHelper.startRecording(getVoiceFileName());
                } else if (sIsRecording && (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL)) {
                    sVoiceHelper.finishRecording();
                    btn_play.setVisibility(View.VISIBLE);
                    sIsRecording = false;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStop() {
        sVoiceHelper.stop();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (sDiscardNote && sVoiceFile.length() > 0) {
            deleteFile(sVoiceFile);
        }

        super.onDestroy();
    }

    private String getVoiceFileName() {
        sVoiceFile = System.currentTimeMillis() + ".3gpp";
        return sVoiceFile;
    }

    public void playVoice(View v) {
        if (sVoiceFile.length() != 0) {
            sVoiceHelper.playVoice(sVoiceFile);
        }
    }

    public void saveNote(View v) {
        String description = ((EditText)findViewById(R.id.et_desc)).getText().toString();
        if (sVoiceFile.length() == 0 && description.length() == 0) {
            Toast.makeText(this, R.string.basic_info_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = (new DbHelper(this).getWritableDatabase());
        Note note = new Note(0, description, sDueDate, sVoiceFile, sCurrentPrio);
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
        sDiscardNote = false;
        finish();
    }

    public void cancelEdit(View v) {
        // destroy recorded voice, etc
        finish();
    }

    private String getTime(int progress) {
        String display = "";
        if (progress == 0) {
            sDueDate = 0;
        } else if (progress > 0 && progress <= 25) {
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
