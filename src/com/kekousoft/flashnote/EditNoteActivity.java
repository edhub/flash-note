
package com.kekousoft.flashnote;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class EditNoteActivity extends Activity {

    public static final String NOTE_ID = "com.kekousoft.flashnote.NOTEID";

    private long mDueDate;

    private boolean mDiscardVoice = true;

    private long mRecordStart;

    private int[] mColors;

    private int mColorIndex = 0;

    private Note mNote;

    private Handler mHandler;

    private String mVoiceFile = "";

    private VoiceHelper mVoiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Resources res = getResources();
        mHandler = new Handler();

        String[] colors = res.getStringArray(R.array.colors);
        mColors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            mColors[i] = Color.parseColor(colors[i]);
        }

        final TextView tv_date = (TextView)findViewById(R.id.tv_date);
        final ImageButton btn_record = (ImageButton)findViewById(R.id.btn_record);

        long id = getIntent().getLongExtra(NOTE_ID, 0);
        if (id > 0) {
            mNote = Model.getNoteById(this, id);
        }

        // Init data for edit mode
        if (mNote != null) {
            for (int i = 0; i < mColors.length; i++) {
                if (mNote.color == mColors[i]) {
                    mColorIndex = i;
                    break;
                }
            }
            if (mNote.voiceRecord.length() > 0) {
                ImageButton btn_play_orig = (ImageButton)findViewById(R.id.btn_play_orig);
                btn_play_orig.setVisibility(View.VISIBLE);
            }
            if (mNote.dueDate > System.currentTimeMillis()) {
                tv_date.setText(DateUtils.getRelativeTimeSpanString(mNote.dueDate));
            }
            if (mNote.description.length() > 0) {
                ((EditText)findViewById(R.id.et_desc)).setText(mNote.description);
            }
        }

        View v_prio = findViewById(R.id.v_prio);
        v_prio.setBackgroundColor(nextColor());
        View lo_desc = findViewById(R.id.lo_desc);
        lo_desc.setBackgroundColor(getColor());

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

        mVoiceHelper = new VoiceHelper(this);
        btn_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mRecordStart == 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
                    mRecordStart = System.currentTimeMillis();
                    if (mVoiceFile.length() > 0) {
                        deleteFile(mVoiceFile);
                    }
                    mVoiceHelper.startRecording(getVoiceFileName());
                } else if (mRecordStart > 0 && (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL)) {
                    stopRecordingSafely();
                }
                return false;
            }
        });
    }

    private void stopRecordingSafely() {
        if (mRecordStart > 0) {
            long timeSpan = System.currentTimeMillis() - mRecordStart;
            final ImageButton btn_play = (ImageButton)findViewById(R.id.btn_play);
            // delete voice file if the recording is less than 0.4 second.
            if (timeSpan < 400) {
                final ImageButton btn_record = (ImageButton)findViewById(R.id.btn_record);
                btn_record.setEnabled(false);
                Toast.makeText(this, "Too short", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mVoiceHelper.finishRecording();
                        deleteFile(mVoiceFile);
                        mVoiceFile = "";
                        btn_play.setVisibility(View.GONE);
                        btn_record.setEnabled(true);
                    }
                }, 300 - timeSpan);
            } else {
                mVoiceHelper.finishRecording();
                btn_play.setVisibility(View.VISIBLE);
            }
            mRecordStart = 0;
        }
    }

    @Override
    protected void onStop() {
        stopRecordingSafely();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mDiscardVoice && mVoiceFile.length() > 0) {
            deleteFile(mVoiceFile);
        }
        super.onDestroy();
    }

    private String getVoiceFileName() {
        mVoiceFile = System.currentTimeMillis() + ".3gpp";
        return mVoiceFile;
    }

    public void playVoice(View v) {
        if (mVoiceFile.length() != 0) {
            mVoiceHelper.playVoice(mVoiceFile);
        }
    }

    public void playVoiceOrig(View v) {
        mVoiceHelper.playVoice(mNote.voiceRecord);
    }

    public void togglePrio(View v) {
        mColorIndex = (mColorIndex + 1) % mColors.length;
        View lo_desc = findViewById(R.id.lo_desc);
        lo_desc.setBackgroundColor(getColor());
        v.setBackgroundColor(nextColor());
    }

    private int nextColor() {
        int next = (mColorIndex + 1) % mColors.length;
        return mColors[next];
    }

    private int getColor() {
        return mColors[mColorIndex];
    }

    public void saveNote(View v) {
        String description = ((EditText)findViewById(R.id.et_desc)).getText().toString();
        if (mNote == null && mVoiceFile.length() == 0 && description.length() == 0) {
            Toast.makeText(this, R.string.basic_info_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mNote != null) {
            mNote.description = description;
            if (mDueDate > 0) {
                mNote.dueDate = mDueDate;
            }
            if (mVoiceFile.length() > 0) {
                deleteFile(mNote.voiceRecord);
                mNote.voiceRecord = mVoiceFile;
            }
            mNote.color = getColor();
            Model.updateNote(this, mNote);
        } else {
            mNote = new Note(0, description, mDueDate, mVoiceFile, getColor(), 0);
            Model.insertNote(this, mNote);
        }
        mDiscardVoice = false;
        finish();
    }

    public void cancelEdit(View v) {
        finish();
    }

    private String getTime(int progress) {
        if (progress == 0) {
            mDueDate = 0;
        } else if (progress > 0 && progress <= 25) {
            float num = progress * 1.0f / 25 * 60;
            mDueDate = (long)(System.currentTimeMillis() + num * 60 * 1000);
        } else if (progress > 25 && progress <= 50) {
            float num = (progress - 24) * 1.0f / 25 * 24;
            mDueDate = (long)(System.currentTimeMillis() + num * 60 * 60 * 1000);
        } else if (progress > 50 && progress <= 75) {
            float num = (progress - 50) * 1.0f / 25 * 30;
            mDueDate = (long)(System.currentTimeMillis() + num * 24 * 60 * 60 * 1000);
        } else if (progress > 75) {
            float num = (progress - 74) * 1.0f / 2;
            mDueDate = (long)(System.currentTimeMillis() + num * 30 * 24 * 60 * 60 * 1000);
        }
        if (mDueDate == 0) {
            return "";
        }
        return DateUtils.getRelativeTimeSpanString(mDueDate).toString();
    }

}
