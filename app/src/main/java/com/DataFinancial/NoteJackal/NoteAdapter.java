package com.DataFinancial.NoteJackal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

//import android.R;

public class NoteAdapter extends BaseAdapter implements ListAdapter {

	private List<Note> notes;
	private Context context;
    private Utils util;
    private DatabaseReminders dbRem;
    private String hasReminder = "false";

	public NoteAdapter(Context context, List<Note> notes) {

        util = new Utils();
		this.notes = notes;
		this.context = context;
        dbRem = new DatabaseReminders(context);
	}

	@Override
	public int getCount() {

		return notes.size();
	}

	@Override
	public Object getItem(int position) {

		return notes.get(position);
	}

	@Override
	public long getItemId(int position) {

		return notes.get(position).getId();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        PlaceHolder holder = null;

        //if we don't currently have a row View to reuse...
		if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_note_item_rel, parent, false);

            holder = new PlaceHolder();
            holder.dateView = (TextView) row.findViewById(R.id.note_date);
            holder.titleView = (TextView) row.findViewById(R.id.note_title);
            holder.noteIconView = (ImageView) row.findViewById(R.id.note_icon);

            row.setTag(holder);
        } else {
            // otherwise use an existing tag
            holder = (PlaceHolder) row.getTag();
        }

        //Get the data from the list of notes
		Note note = notes.get(position);

	    Integer  rowPosition = position;
        holder.noteIconView.setTag(rowPosition);
		String editDate = util.convertDate(note.getEditDate(), "yy/MM/dd", "MM/dd/yy");
        String createDate = util.convertDate(note.getCreateDate(), "yy/MM/dd", "MM/dd/yy");

		String title = note.getBody();
		String strHelp = (String) this.context.getResources().getText(R.string.txt_help_search);
		Boolean help = title.substring(0, strHelp.length()).equals(strHelp);
		
		if (!help) {
			if (title.length() > 75) {
				title = title.substring(0,75) + "...";
				String[] s;
				if ((s = title.split("\n")).length > 2) {        	
					title = s[0] + "\n" + s[1] + "...";
				}
			}	
		} else {
			title = note.getBody().substring(0,note.getBody().indexOf('/')-1);
		}

		String dateHeader;
		if (editDate == null) {
			dateHeader = "Created: " + createDate;
		} else {
			dateHeader = "Created: " + createDate + "       Edited: " + editDate;
		}

		//reminder = null;
		hasReminder = note.getHasReminder();
      
        Drawable noteIcon;
        int image_code = (note.getImage().isEmpty() ? 0 : 1) + (note.getLatitude().isEmpty() ? 0 : 1)*2 + note.getPriority()*4 + (hasReminder.equals("false") ? 0 : 1)*8 + (help ? 16 : 0);
		//int image_code = note.getPriority() + (note.getLatitude().isEmpty() ? 0 : 1)*2 + (hasReminder.equals("false") ? 0 : 1)*4 + (note.getImage().isEmpty() ? 0 : 8) + (help ? 16 : 0);
		switch(image_code) {
		case 0:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad);
			break;
		case 1:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_i);
			break;
		case 2:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_g);
			break;
		case 3:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_ig);
			break;
		case 4:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_p);
			break;
		case 5:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_ip);
			break;
		case 6:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_pg);
			break;
		case 7:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_igp);
			break;
		case 8:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad_r);
			break;
        case 9:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_ir);
            break;
        case 10:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_gr);
            break;
        case 11:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_igr);
            break;
        case 12:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_pr);
            break;
        case 13:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_ipr);
            break;
        case 14:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_gpr);
            break;
        case 15:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_igpr);
            break;
        case 16:
            noteIcon = context.getResources().getDrawable(R.drawable.notepad_q);
            break;
  		default:
			noteIcon = context.getResources().getDrawable(R.drawable.notepad);
			break;
		}

		holder.noteIconView.setImageDrawable(noteIcon);
		holder.dateView.setText(dateHeader);
	
		if (!help) {
			holder.titleView.setText(title);
		} else {
			holder.titleView.setText(Html.fromHtml(title));
		}

		return row;
	}


    private static class PlaceHolder {
        TextView dateView;
        TextView titleView ;
        ImageView noteIconView;
    }

}
