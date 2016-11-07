package styshak.com.taskslist.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import styshak.com.taskslist.R;
import styshak.com.taskslist.objects.TodoDocument;

public class TodoAdapter extends ArrayAdapter<TodoDocument> {

    private View.OnClickListener checkListener;

    public TodoAdapter(Context context, int resource, List<TodoDocument> listDocuments, View.OnClickListener checkListener) {
        super(context, resource, listDocuments);
        this.checkListener = checkListener;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_listview_row,
                    parent, false);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.todoName = (TextView) convertView.findViewById(R.id.todo_name);
            viewHolder.todoDate = (TextView) convertView.findViewById(R.id.todo_date);
            viewHolder.imagePriority = (ImageView) convertView.findViewById(R.id.image_priority);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.selectedRow);
            convertView.setTag(viewHolder);
            viewHolder.checkBox.setOnClickListener(checkListener);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        TodoDocument todoDocument = getItem(position);
        holder.todoName.setText(todoDocument.getName());
        holder.todoDate.setText(DateFormat.format("dd MMMM yyyy,  hh:mm", todoDocument.getCreateDate()));
        if(todoDocument.isChecked()) {
            holder.todoName.setTextColor(Color.LTGRAY);
            holder.todoDate.setTextColor(Color.LTGRAY);
        } else {
            holder.todoName.setTextColor(Color.BLACK);
            holder.todoDate.setTextColor(Color.BLACK);
        }
        holder.checkBox.setChecked(todoDocument.isChecked());
        switch (todoDocument.getPriorityType()) {
            case HIGH:
                holder.imagePriority.setImageResource(R.drawable.ic_priority_high);
                break;
            case MIDDLE:
                holder.imagePriority.setImageResource(R.drawable.ic_priority_middle);
                break;
            case LOW:
                holder.imagePriority.setImageResource(R.drawable.ic_priority_low);
                break;
            default:
                break;
        }
        todoDocument.setChecked(false);
        holder.checkBox.setTag(todoDocument);
        return convertView;
    }

    static class ViewHolder {
        public TextView todoName;
        public TextView todoDate;
        public ImageView imagePriority;
        public CheckBox checkBox;
    }
}
