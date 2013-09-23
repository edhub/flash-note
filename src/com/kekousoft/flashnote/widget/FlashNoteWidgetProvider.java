
package com.kekousoft.flashnote.widget;

import com.kekousoft.flashnote.EditNoteActivity;
import com.kekousoft.flashnote.MainActivity;
import com.kekousoft.flashnote.R;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class FlashNoteWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_NEW = "action_new";

    public static final String ACTION_MANAGE = "action_manage";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, WidgetViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            rv.setRemoteAdapter(R.id.lv_notes, intent);

            Intent ser = new Intent(context, WidgetEventHandleService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, ser,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.lv_notes, pi);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
    }

    public static class WidgetEventHandleService extends IntentService {

        public WidgetEventHandleService() {
            super("WidgetEventHandleService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            if (intent.hasExtra(ACTION_NEW)) {
                Intent i = new Intent(this, EditNoteActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else if (intent.hasExtra(ACTION_MANAGE)) {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }
    }
}
