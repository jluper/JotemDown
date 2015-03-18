package com.DataFinancial.NoteJackal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;


public class GroupsAdapter extends BaseAdapter implements ListAdapter {

    private List<NoteGroup> groups;
    private Context context;
    private Utils util;


    public GroupsAdapter(Context context, List<NoteGroup> groups) {

        util = new Utils();
        this.groups = groups;
        this.context = context;
    }

    @Override
    public int getCount() {

        return groups.size();
    }

    @Override
    public Object getItem(int position) {

        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {

        return groups.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        PlaceHolder holder = null;
        //if we don't currently have a row View to reuse...
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_group_view, parent, false);

            holder = new PlaceHolder();
            holder.groupIconView = (ImageView) row.findViewById(R.id.group_row_image);
            holder.groupTextView = (TextView) row.findViewById(R.id.group_row_text);

            row.setTag(holder);
        } else {
            // otherwise use an existing tag
            holder = (PlaceHolder) row.getTag();
        }
        //Get the data from the list of notes
        NoteGroup group  = groups.get(position);
        Integer  rowPosition = position;
        holder.groupIconView.setTag(rowPosition);
        Drawable groupIcon;
        groupIcon = context.getResources().getDrawable(R.drawable.folder);
        holder.groupIconView.setImageDrawable(groupIcon);
        holder.groupTextView.setText(group.getName());
        return row;
    }

    private static class PlaceHolder {
        TextView groupTextView;
        ImageView groupIconView;
    }

}

