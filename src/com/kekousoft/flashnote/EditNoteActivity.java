
package com.kekousoft.flashnote;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class EditNoteActivity extends Activity {

    private long sDueDate;

    private boolean sDiscardNote = true;

    private long sRecordStart;

    private int[] sColors;

    private int sColorIndex = 0;

    private Handler sHandler;

    private String sVoiceFile = "";

    private VoiceHelper sVoiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Resources res = getResources();
        String[] colors = res.getStringArray(R.array.colors);
        sColors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            sColors[i] = Color.parseColor(colors[i]);
        }

        sHandler = new Handler();

        View v_prio = findViewById(R.id.v_prio);
        v_prio.setBackgroundColor(nextColor());

        View lo_desc = findViewById(R.id.lo_desc);
        lo_desc.setBackgroundColor(getColor());

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
        final ImageButton btn_record = (ImageButton)findViewById(R.id.btn_record);
        btn_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (sRecordStart == 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
                    sRecordStart = System.currentTimeMillis();
                    if (sVoiceFile.length() > 0) {
                        deleteFile(sVoiceFile);
                    }
                    sVoiceHelper.startRecording(getVoiceFileName());
                } else if (sRecordStart > 0 && (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL)) {
                    stopRecordingSafely();
                }
                return false;
            }
        });
    }

    private void stopRecordingSafely() {
        if (sRecordStart > 0) {
            long timeSpan = System.currentTimeMillis() - sRecordStart;
            final ImageButton btn_play = (ImageButton)findViewById(R.id.btn_play);
            // delete voice file if the recording is less than 0.5 second.
            if (timeSpan < 500) {
                final ImageButton btn_record = (ImageButton)findViewById(R.id.btn_record);
                btn_record.setEnabled(false);
                Toast.makeText(this, "Too short", Toast.LENGTH_SHORT).show();
                sHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sVoiceHelper.finishRecording();
                        deleteFile(sVoiceFile);
                        sVoiceFile = "";
                        btn_play.setVisibility(View.GONE);
                        btn_record.setEnabled(true);
                    }
                }, 300 - timeSpan);
            } else {
                sVoiceHelper.finishRecording();
                btn_play.setVisibility(View.VISIBLE);
            }
            sRecordStart = 0;
        }
    }

    @Override
    protected void onStop() {
        stopRecordingSafely();
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

    public void togglePrio(View v) {
        sColorIndex = (sColorIndex + 1) % sColors.length;
        View lo_desc = findViewById(R.id.lo_desc);
        lo_desc.setBackgroundColor(getColor());
        v.setBackgroundColor(nextColor());
    }

    private int nextColor() {
        int next = (sColorIndex + 1) % sColors.length;
        return sColors[next];
    }

    private int getColor() {
        return sColors[sColorIndex];
    }

    public void saveNote(View v) {
        String description = ((EditText)findViewById(R.id.et_desc)).getText().toString();
        if (sVoiceFile.length() == 0 && description.length() == 0) {
            Toast.makeText(this, R.string.basic_info_missing, Toast.LENGTH_SHORT).show();
            return;
        }
        Note note = new Note(0, description, sDueDate, sVoiceFile, getColor(), 0);
        Model.insertNote(this, note);
        sDiscardNote = false;
        finish();
    }

    public void cancelEdit(View v) {
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

}
