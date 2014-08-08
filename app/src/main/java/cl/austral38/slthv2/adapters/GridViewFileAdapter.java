package cl.austral38.slthv2.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import cl.austral38.slthv2.R;
import cl.austral38.slthv2.models.FileStorage;

/**
 * Created by eDelgado on 02-06-14.
 */
public class GridViewFileAdapter extends ArrayAdapter<String> {

    Context context;

    int layoutResourceId;
    ArrayList<String> data = new ArrayList<String>();

    public GridViewFileAdapter(Context context, int layoutResourceId,
                               ArrayList<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;

    }


    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
    CheckableLayout l;
        View row = convertView;

        RecordHolder holder = null;

        if (row == null) {
            l = new CheckableLayout(context);
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new RecordHolder();
            holder.txtTitle = (TextView) row.findViewById(R.id.file_description);

            holder.imageItem = (ImageView) row.findViewById(R.id.icon_file);

            row.setTag(holder);


            holder.txtTitle.setText(data.get(position));
            holder.imageItem.setImageResource(R.drawable.ic_excel);
            l.setLayoutParams(new GridView.LayoutParams(
                    GridView.LayoutParams.FILL_PARENT,
                    GridView.LayoutParams.FILL_PARENT));

            l.addView(row);

        } else {

            holder = (RecordHolder) row.getTag();
            l = (CheckableLayout) convertView;

        }

        return l;

    }

    static class RecordHolder {

        TextView txtTitle;

        ImageView imageItem;

    }

    public class CheckableLayout extends FrameLayout implements Checkable {
        private boolean mChecked;

        public CheckableLayout(Context context) {
            super(context);
        }

        @SuppressWarnings("deprecation")
        public void setChecked(boolean checked) {
            mChecked = checked;
            setBackgroundDrawable(checked ? getResources().getDrawable(
                    R.drawable.selector) : null);
        }

        public boolean isChecked() {
            return mChecked;
        }

        public void toggle() {
            setChecked(!mChecked);
        }

    }

}

