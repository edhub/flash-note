
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void newNote(MenuItem mi) {
        Intent i = new Intent(MainActivity.this, EditNoteActivity.class);
        startActivity(i);
    }

    public void showOngoing(MenuItem mi) {
        sAdapter.changeModel(Model.ONGOING);
    }

    public void showDone(MenuItem mi) {
        sAdapter.changeModel(Model.DONE);
    }

    public void showAll(MenuItem mi) {
        sAdapter.changeModel(Model.ALL);
    }

    public void toggleEdit(MenuItem mi) {
        sAdapter.toggleEdit();
    }

    @Override
    protected void onStop() {
        sVoiceHelper.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
