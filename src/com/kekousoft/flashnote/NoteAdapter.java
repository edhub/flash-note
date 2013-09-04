
package com.kekousoft.flashnote;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteAdapter extends BaseAdapter implements Model.DataChangeObserver {

    private ArrayList<Note> sNotes;

    private LayoutInflater sInflater;

    public NoteAdapter(Context context, Model model) {
        sInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sNotes = model.getNotes();
        model.registerObserver(this);
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
        tv_duedate.setText(DateUtils.getRelativeTimeSpanString(note.dueDate));

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
