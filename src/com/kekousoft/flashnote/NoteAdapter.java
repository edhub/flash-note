
package com.kekousoft.flashnote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteAdapter extends BaseAdapter implements Model.DataChangeObserver {

    private ArrayList<Note> sNotes;

    private LayoutInflater sInflater;

    private boolean sEdit = false;

    private Model sModel;

    private VoiceHelper sVoiceHelper;

    private Context sContext;

    public NoteAdapter(Context context, Model model, VoiceHelper voiceHelper) {
        sContext = context;
        sVoiceHelper = voiceHelper;
        sInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sModel = model;
        sNotes = sModel.getNotes(Model.ONGOING);
        model.registerObserver(this);
    }

    public void changeModel(int which) {
        sNotes = sModel.getNotes(which);
        notifyDataSetChanged();
    }

    public void toggleEdit() {
        sEdit = !sEdit;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sNotes.size();
    }

    @Override
    public Object getItem(int position) {
        return sNotes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return sNotes.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = sInflater.inflate(R.layout.note_item, null);
        }
        final Note note = sNotes.get(position);

        view.setBackgroundColor(note.color);

        TextView tv_desc = (TextView)view.findViewById(R.id.tv_desc);
        tv_desc.setText(note.description);

        TextView tv_date = (TextView)view.findViewById(R.id.tv_date);
        if (note.finishedOn > 0) {
            tv_date.setText(sContext.getString(R.string.finished_on)
                    + DateUtils.getRelativeTimeSpanString(note.finishedOn));
        } else if (note.dueDate > 0) {
            tv_date.setText(DateUtils.getRelativeTimeSpanString(note.dueDate));
        } else {
            tv_date.setText("");
        }

        ImageButton btn_play_voice = (ImageButton)view.findViewById(R.id.btn_play_voice);
        if (note.voiceRecord.length() != 0) {
            btn_play_voice.setVisibility(View.VISIBLE);
            btn_play_voice.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sVoiceHelper.playVoice(note.voiceRecord);
                }
            });
        } else {
            btn_play_voice.setVisibility(View.GONE);
        }

        ImageButton btn_finish = (ImageButton)view.findViewById(R.id.btn_finish);
        if (note.finishedOn == 0) {
            btn_finish.setImageResource(R.drawable.finish);
            btn_finish.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sModel.finishNote(sContext, note);
                }
            });
        } else {
            btn_finish.setImageResource(R.drawable.reopen);
            btn_finish.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sModel.reOpenNote(sContext, note);
                }
            });
        }

        View lo_edit = view.findViewById(R.id.lo_edit);
        lo_edit.setTag(note.id);
        if (sEdit) {
            lo_edit.setVisibility(View.VISIBLE);
            ImageButton btn_edit = (ImageButton)view.findViewById(R.id.btn_edit);
            btn_edit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(sContext, EditNoteActivity.class);
                    i.putExtra("noteid", note.id);
                    sContext.startActivity(i);
                }
            });

            ImageButton btn_delete = (ImageButton)view.findViewById(R.id.btn_delete);
            btn_delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(sContext);
                    alert.setMessage(sContext.getText(R.string.confirm_delete) + note.description);
                    alert.setCancelable(true);
                    alert.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sModel.deleteNote(sContext, note);
                                }
                            });
                    alert.setNegativeButton(android.R.string.cancel, null);
                    alert.show();

                }
            });

        } else {
            lo_edit.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void notifyModelChanged() {
        notifyDataSetChanged();
    }

}
