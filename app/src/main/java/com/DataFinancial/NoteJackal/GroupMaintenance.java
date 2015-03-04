package com.DataFinancial.NoteJackal;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;


public class GroupMaintenance extends ActionBarActivity {

    private DatabaseNotes db = new DatabaseNotes(this);
    private ListView groupList;
    //private NoteGroup selectedGroup;
    EditText groupEditText;
    NoteGroup currentGroup;
    List<NoteGroup> grps;
    GroupAdapter grpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_maintenance);
        Log.d(MainActivity.DEBUGTAG, "check 0.1");
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle(getResources().getString(R.string.title_groups));
        actionBar.setDisplayShowTitleEnabled(true);

        groupEditText = (EditText) findViewById(R.id.editview_group);
        groupEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(16) });
        groupList = (ListView) findViewById(R.id.group_list_maint);
        populategroupList();
    }


    private void populategroupList() {

        grps = db.getGroups(DatabaseNotes.COL_ID, "ASC");
        Log.d(MainActivity.DEBUGTAG, "grps = " +  grps.toString());
        grpAdapter = new GroupAdapter(this, grps);
        Log.d(MainActivity.DEBUGTAG, "grpAdapter = " + grpAdapter.toString());
        groupList.setAdapter(grpAdapter);

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long arg3) {

                currentGroup = (NoteGroup) adapter.getItemAtPosition(pos);
                //Log.d(MainActivity.DEBUGTAG, "pos = " + pos + " grp id = " + grp.getId() + " grp name = " + grp.getName());
                groupList.setItemChecked(pos, true);
                groupEditText.setText(currentGroup.getName());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

            if (id == R.id.menu_new_group) {

                String grpName = groupEditText.getText().toString();
                if (grpName.length() > 0) {
                    for (NoteGroup grp : grps) {
                        if (grp.getName().equals(grpName)) {
                            Toast.makeText(GroupMaintenance.this, "Can't add the folder because it already exists.", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                    NoteGroup newGroup = new NoteGroup(grpName);
                    db.addGroup(newGroup);
                    groupEditText.setText("");
                    populategroupList();
                }
            }

        if (id == R.id.menu_delete_group) {

            String grpName = groupEditText.getText().toString();
            if (grpName.length() > 0) {
                for (NoteGroup grp : grps) {
                    if (grp.getName().equals(grpName)) {

                        db.deleteGroup(grp.getId());
                        groupEditText.setText("");
                        populategroupList();
                        return true;
                    }
                }

                Toast.makeText(GroupMaintenance.this, "Can't delete the folder because it doesn't exist.", Toast.LENGTH_LONG).show();

            }
        }

        if (id == R.id.menu_edit_group) {

            String grpName = groupEditText.getText().toString();
            NoteGroup groupSelected = (NoteGroup) groupList.getItemAtPosition(groupList.getCheckedItemPosition());
            Log.d(MainActivity.DEBUGTAG, "groupSelected = " + groupSelected.getName() + "grpName = " + grpName);
            if (groupSelected != null) {
                if (!grpName.isEmpty()) {
                    groupSelected.setName(groupEditText.getText().toString());
                    Log.d(MainActivity.DEBUGTAG, "new name = " + groupSelected.getName());
                    db.updateGroup(groupSelected);
                    groupEditText.setText("");
                    populategroupList();
                    return true;
                }

                Toast.makeText(GroupMaintenance.this, "Can't edit group because name is invalid.", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
