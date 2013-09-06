
package com.kekousoft.flashnote;

import com.kekousoft.flashnote.Controller.DataChangeObserver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;

public class AlarmMaker implements DataChangeObserver {
    private static final boolean DEBUG = false;

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
            if (intent.hasExtra(SETUP_ALARM)) {
                setupNextAlarm(context);
            } else if (intent.hasExtra(EXTRA_DESC)) {
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
                setupNextAlarm(context);
            }
        }

        private void setupNextAlarm(Context context) {
            SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
            Cursor c = db.query(
                    Note.TABLE_NAME,
                    new String[] {
                            Note.COL_DUEDATE, Note.COL_DESC
                    },
                    Note.COL_FINISHED_ON + "=0 and " + Note.COL_DUEDATE + ">"
                            + System.currentTimeMillis(), null, null, null, Note.COL_DUEDATE
                            + " asc", "1");
            if (c.moveToFirst()) {
                int col_due = c.getColumnIndex(Note.COL_DUEDATE);
                int col_desc = c.getColumnIndex(Note.COL_DESC);
                long dueDate = c.getLong(col_due);
                if (DEBUG) {
                    dueDate = System.currentTimeMillis() + 10000;
                }
                Intent i = new Intent(context, AlarmReceiver.class);
                i.putExtra(EXTRA_DESC, c.getString(col_desc));
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.RTC, dueDate, pi);
            } else {
                Intent ci = new Intent(context, AlarmReceiver.class);
                ci.putExtra(EXTRA_DESC, "");
                PendingIntent cpi = PendingIntent.getBroadcast(context, 0, ci,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                am.cancel(cpi);
            }
            c.close();
            db.close();
        }
    }

    @Override
    public void notifyModelChanged() {
        Intent i = new Intent(sAppContext, AlarmReceiver.class);
        i.putExtra(AlarmMaker.SETUP_ALARM, "");
        sAppContext.sendBroadcast(i);
    }
}
