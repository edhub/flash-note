
package com.kekousoft.flashnote;

import com.kekousoft.flashnote.alarm.AlarmMaker.AlarmReceiver;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends Activity {

    private VoiceHelper mVoiceHelper;

    private NoteAdapter mAdapter;

    private Controller mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceHelper = new VoiceHelper(this);
        mController = Controller.getInstance(this, true);
        mAdapter = new NoteAdapter(this, mController, mVoiceHelper);
        mController.registerObserver(mAdapter);

        ListView lv_notes = (ListView)findViewById(R.id.lv_notes);
        lv_notes.setAdapter(mAdapter);

        // Always update alarm on startup
        Intent i = new Intent(this, AlarmReceiver.class);
        sendBroadcast(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.hideDelete();
    }

    @Override
    protected void onStop() {
        mVoiceHelper.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mController.unRegisterObserver(mAdapter);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void newNote(MenuItem mi) {
        Intent i = new Intent(MainActivity.this, EditNoteActivity.class);
        startActivity(i);
    }

    public void showOngoing(MenuItem mi) {
        mAdapter.changeModel(Controller.MODEL_ONGOING);
    }

    public void showDone(MenuItem mi) {
        mAdapter.changeModel(Controller.MODEL_DONE);
    }

    public void showAll(MenuItem mi) {
        mAdapter.changeModel(Controller.MODEL_ALL);
    }

    public void toggleDelete(MenuItem mi) {
        mAdapter.toggleDelete();
    }

}
