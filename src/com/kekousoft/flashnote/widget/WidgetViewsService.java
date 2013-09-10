
package com.kekousoft.flashnote.widget;

import com.kekousoft.flashnote.Controller;
import com.kekousoft.flashnote.Note;
import com.kekousoft.flashnote.R;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

public class WidgetViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetViewsFactory(this.getApplicationContext(), intent);
    }

    private static class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory,
            Controller.DataChangeObserver {

        private Context mContext;

        private int mAppWidgetId;

        private final String mNotScheduled;

        private final String mNoDescription;

        private ArrayList<Note> mModel;

        public WidgetViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            mNoDescription = mContext.getString(R.string.no_description);
            mNotScheduled = mContext.getString(R.string.not_scheduled);
        }

        @Override
        public int getCount() {
            return mModel.size() + 1;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return 0;
            }
            return mModel.get(position - 1).id;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == 0) {
                RemoteViews views = new RemoteViews(mContext.getPackageName(),
                        R.layout.widget_buttons);

                Intent in = new Intent();
                in.putExtra(FlashNoteWidgetProvider.ACTION_NEW, 0);
                views.setOnClickFillInIntent(R.id.lo_item, in);

                return views;
            }

            Note note = mModel.get(position - 1);

            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
            views.setInt(R.id.lo_item, "setBackgroundColor", note.color);
            Intent im = new Intent();
            im.putExtra(FlashNoteWidgetProvider.ACTION_MANAGE, 0);
            views.setOnClickFillInIntent(R.id.lo_item, im);

            if (note.description.length() > 0) {
                views.setTextViewText(R.id.tv_desc, note.description);
            } else {
                views.setTextViewText(R.id.tv_desc, mNoDescription);
            }

            if (note.dueDate > 0) {
                views.setTextViewText(R.id.tv_date,
                        DateUtils.getRelativeTimeSpanString(note.dueDate));
            } else {
                views.setTextViewText(R.id.tv_date, mNotScheduled);
            }

            return views;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onCreate() {
            Controller ctrl = Controller.getInstance(mContext, true);
            ctrl.registerObserver(this);
            mModel = ctrl.getModel(Controller.MODEL_ONGOING);
        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
            Controller ctrl = Controller.getInstance(mContext, true);
            ctrl.unRegisterObserver(this);
        }

        @Override
        public void notifyModelChanged() {
            AppWidgetManager.getInstance(mContext)
                    .notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.lv_notes);
        }
    }
}
