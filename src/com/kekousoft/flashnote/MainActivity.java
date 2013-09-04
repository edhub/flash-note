
package com.kekousoft.flashnote;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends Activity {

    private VoiceHelper sVoiceHelper;

    private NoteAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sVoiceHelper = new VoiceHelper(this);
        sAdapter = new NoteAdapter(this, Model.getModel(this, true), sVoiceHelper);

        ListView lv_notes = (ListView)findViewById(R.id.lv_notes);
        lv_notes.setAdapter(sAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void newNote(MenuItem mi) {
        Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
        startActivity(i);
    }

    public void showUpcoming(MenuItem mi) {
        sAdapter.changeModel(Model.UPCOMING);
    }

    public void showPast(MenuItem mi) {
        sAdapter.changeModel(Model.PAST);
    }

    public void showNoDuedate(MenuItem mi) {
        sAdapter.changeModel(Model.NO_DUEDATE);
    }

    public void showAll(MenuItem mi) {
        sAdapter.changeModel(Model.ALL);
    }

    @Override
    protected void onStop() {
        sVoiceHelper.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Model.releaseModel();
        super.onDestroy();
    }
}
