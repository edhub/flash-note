
package com.kekousoft.flashnote;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteAdapter extends BaseAdapter implements Model.DataChangeObserver {

    private ArrayList<Note> sNotes;

    private LayoutInflater sInflater;

    private Model sModel;

    private VoiceHelper sVoiceHelper;

    public NoteAdapter(Context context, Model model, VoiceHelper voiceHelper) {
        sVoiceHelper = voiceHelper;
        sInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sModel = model;
        sNotes = sModel.getNotes(Model.UPCOMING);
        model.registerObserver(this);
    }

    public void changeModel(int which) {
        sNotes = sModel.getNotes(which);
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
        Note note = sNotes.get(position);

        TextView tv_desc = (TextView)view.findViewById(R.id.tv_desc);
        tv_desc.setText(note.description);
        tv_desc.setTextColor(getPrioColor(note.priority));

        TextView tv_duedate = (TextView)view.findViewById(R.id.tv_duedate);
        if (note.dueDate != 0) {
            tv_duedate.setText(DateUtils.getRelativeTimeSpanString(note.dueDate));
        } else {
            tv_duedate.setText("");
        }

        final String voiceRecord = note.voiceRecord;

        if (voiceRecord.length() != 0) {
            Button btn_play_voice = (Button)view.findViewById(R.id.btn_play_voice);
            btn_play_voice.setVisibility(View.VISIBLE);
            btn_play_voice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sVoiceHelper.playVoice(voiceRecord);
                }
            });
        }

        return view;
    }

    private int getPrioColor(int priority) {
        switch (priority) {
            case Note.PRIO_LOW:
                return Color.GRAY;
            case Note.PRIO_HIGH:
                return Color.RED;
            default:
                return Color.CYAN;
        }
    }

    @Override
    public void notifyModelChanged() {
        notifyDataSetChanged();
    }

}
