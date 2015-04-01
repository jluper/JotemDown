package com.DataFinancial.JotemDown;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;


public class GroupMaintenance extends ActionBarActivity {

    private DatabaseNotes db = new DatabaseNotes(this);
    private ListView groupList;
    //private NoteGroup selectedGroup;
    EditText groupEditText;
    NoteGroup currentGroup;
    List<NoteGroup> grps;
    GroupsAdapter grpAdapter;
    private int group;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_maintenance);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle(getResources().getString(R.string.title_groups));
        actionBar.setDisplayShowTitleEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = (extras.getInt("group"));
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }

        groupEditText = (EditText) findViewById(R.id.editview_group);
        groupEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(21)});
        groupList = (ListView) findViewById(R.id.group_list_maint);
        populategroupList();
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(GroupMaintenance.this, MainActivity.class);

        i.putExtra("group", group);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;

    }

    private void populategroupList() {

        grps = db.getGroups(DatabaseNotes.COL_ID, "ASC");

        grpAdapter = new GroupsAdapter(this, grps);

        groupList.setAdapter(grpAdapter);

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long arg3) {

                currentGroup = (NoteGroup) adapter.getItemAtPosition(pos);
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

            NoteGroup deleteGrp = null;
            String action = "delete";
            String grpName = groupEditText.getText().toString();
            if (grpName.length() > 0) {

                // check if there is a group by that name
                for (NoteGroup grp : grps) {
                    if (grp.getName().equals(grpName)) {
                        deleteGrp = grp;
                        break;
                    }
                }

                //if there is such a group then make sure there are no notes in that group
                if (deleteGrp != null) {
                    List<Note> notes;
                    notes = db.getNotesByGroupId(deleteGrp.getId(), DatabaseNotes.COL_GROUP, "ASC");
                    if (notes.size() == 0) {
                        db.deleteGroup(deleteGrp.getId());
                        groupEditText.setText("");
                        populategroupList();
                        return true;
                    } else {
                        action = "Can't delete the folder because it contains notes.";
                    }
                } else {
                    action = "Can't delete the folder because it doesn't exist.";
                }

                Toast.makeText(GroupMaintenance.this, action, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (id == R.id.menu_edit_group) {

            String grpName = groupEditText.getText().toString();
            NoteGroup groupSelected = (NoteGroup) groupList.getItemAtPosition(groupList.getCheckedItemPosition());
            if (groupSelected != null) {
                if (!grpName.isEmpty()) {

                    for (NoteGroup grp : grps) {
                        if (grp.getName().equals(grpName)) {
                            Toast.makeText(GroupMaintenance.this, "Can't use that name because it already exists.", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                    groupSelected.setName(groupEditText.getText().toString());
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

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // enable visible icons in action bar
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Field field = menu.getClass().
                            getDeclaredField("mOptionalIconsVisible");
                    field.setAccessible(true);
                    field.setBoolean(menu, true);
                } catch (IllegalAccessException | NoSuchFieldException e) {

                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }
}
