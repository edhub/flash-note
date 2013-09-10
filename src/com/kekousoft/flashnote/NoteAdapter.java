
package com.kekousoft.flashnote;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
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

public class NoteAdapter extends BaseAdapter implements Controller.DataChangeObserver {

    private ArrayList<Note> mModel;

    private LayoutInflater mInflater;

    private boolean mShowDelete = false;

    private int mCurrentModel = Controller.MODEL_ONGOING;

    private final String mFinishedOn;

    private final String mNoDesc;

    private Controller mController;

    private VoiceHelper mVoiceHelper;

    private Context mContext;

    public NoteAdapter(Context context, Controller model, VoiceHelper voiceHelper) {
        mContext = context;
        mVoiceHelper = voiceHelper;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mController = model;
        mModel = mController.getModel(Controller.MODEL_ONGOING);

        mFinishedOn = mContext.getString(R.string.finished_on);
        mNoDesc = mContext.getString(R.string.no_description);
    }

    public void changeModel(int which) {
        mModel = mController.getModel(which);
        mCurrentModel = which;
        notifyDataSetChanged();
    }

    public void toggleDelete() {
        mShowDelete = !mShowDelete;
        notifyDataSetChanged();
    }

    public void hideDelete() {
        if (mShowDelete) {
            toggleDelete();
        }
    }

    @Override
    public int getCount() {
        return mModel.size();
    }

    @Override
    public Object getItem(int position) {
        return mModel.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mModel.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.note_item, null);
        }
        final Note note = mModel.get(position);

        view.setAlpha(1f);
        view.setBackgroundColor(note.color);

        view.findViewById(R.id.lo_desc).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, EditNoteActivity.class);
                i.putExtra(EditNoteActivity.NOTE_ID, note.id);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(i);
            }
        });

        TextView tv_desc = (TextView)view.findViewById(R.id.tv_desc);
        if (note.description.length() > 0) {
            tv_desc.setText(note.description);
        } else {
            tv_desc.setText(mNoDesc);
        }

        if (note.finishedOn > 0) {
            tv_desc.getPaint().setStrikeThruText(true);
        } else {
            tv_desc.getPaint().setStrikeThruText(false);
        }

        TextView tv_date = (TextView)view.findViewById(R.id.tv_date);
        String dateStr = "";
        if (note.dueDate > 0) {
            dateStr = DateUtils.getRelativeTimeSpanString(note.dueDate).toString();
            tv_date.setText(dateStr);
        }
        if (note.finishedOn > 0) {
            dateStr += String.format(mFinishedOn,
                    DateUtils.getRelativeTimeSpanString(note.finishedOn));
        }
        if (dateStr.length() > 0) {
            tv_date.setText(dateStr);
        } else {
            tv_date.setText(R.string.not_scheduled);
        }

        ImageButton btn_play_voice = (ImageButton)view.findViewById(R.id.btn_play_voice);
        if (note.voiceRecord.length() != 0) {
            btn_play_voice.setVisibility(View.VISIBLE);
            btn_play_voice.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVoiceHelper.playVoice(note.voiceRecord);
                }
            });
        } else {
            btn_play_voice.setVisibility(View.GONE);
        }

        final ImageButton btn_finish = (ImageButton)view.findViewById(R.id.btn_finish);
        if (note.finishedOn == 0) {
            btn_finish.setImageResource(R.drawable.ic_checkbox);
            btn_finish.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurrentModel == Controller.MODEL_ONGOING) {
                        btn_finish.setImageResource(R.drawable.ic_checkbox_checked);
                        btn_finish.setEnabled(false);
                        view.animate().alpha(0f).setDuration(200)
                                .setListener(new AnimatorListener() {
                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        btn_finish.setEnabled(true);
                                        mController.finishNote(mContext, note);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                    }

                                });
                    } else {
                        mController.finishNote(mContext, note);
                    }
                }
            });
        } else {
            btn_finish.setImageResource(R.drawable.ic_checkbox_checked);
            btn_finish.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurrentModel == Controller.MODEL_DONE) {
                        btn_finish.setImageResource(R.drawable.ic_checkbox);
                        btn_finish.setEnabled(false);
                        view.animate().alpha(0).setDuration(300)
                                .setListener(new AnimatorListener() {
                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        btn_finish.setEnabled(true);
                                        mController.reOpenNote(mContext, note);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                    }

                                });
                    } else {
                        mController.reOpenNote(mContext, note);
                    }
                }
            });
        }

        final ImageButton btn_delete = (ImageButton)view.findViewById(R.id.btn_delete);
        if (mShowDelete) {
            btn_delete.setVisibility(View.VISIBLE);
            btn_delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setMessage(mContext.getText(R.string.confirm_delete) + note.description);
                    alert.setCancelable(true);
                    alert.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    btn_delete.setEnabled(false);
                                    view.animate().alpha(0).setDuration(500)
                                            .setListener(new AnimatorListener() {
                                                @Override
                                                public void onAnimationCancel(Animator animation) {
                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    btn_delete.setEnabled(true);
                                                    mController.deleteNote(mContext, note);
                                                }

                                                @Override
                                                public void onAnimationRepeat(Animator animation) {
                                                }

                                                @Override
                                                public void onAnimationStart(Animator animation) {
                                                }

                                            });

                                }
                            });
                    alert.setNegativeButton(android.R.string.cancel, null);
                    alert.show();
                }
            });
        } else {
            btn_delete.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void notifyModelChanged() {
        notifyDataSetChanged();
    }
}
