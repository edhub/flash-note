
package com.kekousoft.flashnote.alarm;

import com.kekousoft.flashnote.Controller;
import com.kekousoft.flashnote.MainActivity;
import com.kekousoft.flashnote.Note;
import com.kekousoft.flashnote.R;
import com.kekousoft.flashnote.Controller.DataChangeObserver;
import com.kekousoft.flashnote.widget.FlashNoteWidgetProvider;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Schedule alarm to keep widgets(if any) updated, or setup notifications for
 * events
 */
public class AlarmMaker implements DataChangeObserver {

    public static final String SETUP_ALARM = "com.kekousoft.flashnote.AlarmMaker.SETUP_ALARM";

    private static final String EXTRA_DESC = "com.kekousoft.flashnote.AlarmMaker.DESC";

    private Context sAppContext;

    /**
     * @param context It's better to use application context cause it'll exists
     *            throughout the life cycle of the application.
     */
    public AlarmMaker(Context appContext) {
        sAppContext = appContext;
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Set up notification for new event
            if (intent.hasExtra(EXTRA_DESC)) {
                Intent i = new Intent(context, MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setSmallIcon(R.drawable.ic_launcher)
                        .setContentText(intent.getStringExtra(EXTRA_DESC))
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentIntent(pi)
                        .setAutoCancel(true);
                NotificationManager nm = (NotificationManager)context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(1, mBuilder.build());
            }

            // Update widgets (if any) to refresh the time displayed
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context,
                    FlashNoteWidgetProvider.class));
            if (ids.length > 0) {
                widgetManager.notifyAppWidgetViewDataChanged(ids, R.id.lv_notes);
            }

            // Make a check to determine next alarm
            Note note = Controller.getUpcomingNote(context);
            if (ids.length == 0 && note == null) {
                cancelAlarm(context);
                return;
            }

            // Set up next alarm
            Intent i = new Intent(context, AlarmReceiver.class);
            long dueDate;
            if (note != null && note.dueDate - System.currentTimeMillis() <= 310 * 1000) {
                i.putExtra(EXTRA_DESC, note.description);
                dueDate = note.dueDate;
            } else {
                dueDate = System.currentTimeMillis() + 300 * 1000;
            }

            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC, dueDate, pi);
        }

        private void cancelAlarm(Context context) {
            Intent ci = new Intent(context, AlarmReceiver.class);
            ci.putExtra(EXTRA_DESC, "");
            PendingIntent cpi = PendingIntent.getBroadcast(context, 0, ci,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(cpi);
        }
    }

    @Override
    public void notifyModelChanged() {
        Intent i = new Intent(sAppContext, AlarmReceiver.class);
        sAppContext.sendBroadcast(i);
    }
}
