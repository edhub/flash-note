
package com.kekousoft.flashnote;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends Activity {

    private VoiceHelper mVoiceHelper;

    private NoteAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceHelper = new VoiceHelper(this);
        mAdapter = new NoteAdapter(this, Model.getModel(this, true), mVoiceHelper);

        ListView lv_notes = (ListView)findViewById(R.id.lv_notes);
        lv_notes.setAdapter(mAdapter);
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
        mAdapter.changeModel(Model.ONGOING);
    }

    public void showDone(MenuItem mi) {
        mAdapter.changeModel(Model.DONE);
    }

    public void showAll(MenuItem mi) {
        mAdapter.changeModel(Model.ALL);
    }

    public void toggleEdit(MenuItem mi) {
        mAdapter.toggleEdit();
    }

    @Override
    protected void onStop() {
        mVoiceHelper.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
