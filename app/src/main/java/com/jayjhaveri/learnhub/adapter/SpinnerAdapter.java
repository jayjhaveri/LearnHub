package com.jayjhaveri.learnhub.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.model.Category;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

/**
 * Created by ADMIN-PC on 22-03-2017.
 */

public class SpinnerAdapter extends ArrayAdapter<Category> {
    int groupid;
    Activity context;
    List<Category> list;
    LayoutInflater inflater;

    public SpinnerAdapter(Activity context, int groupid, int id, List<Category>
            list) {
        super(context, id, list);
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.groupid = groupid;
    }

    @NonNull

    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View itemView = inflater.inflate(groupid, parent, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.iv_category_image);
        imageView.setImageDrawable(
                new IconicsDrawable(context)
                        .icon(list.get(position).getImageResource())
                        .sizeDp(24)
        );
        imageView.setContentDescription(list.get(position).getCategoryName());
        TextView textView = (TextView) itemView.findViewById(R.id.tv_category_name);
        textView.setText(list.get(position).getCategoryName());
        return itemView;
    }

    public View getDropDownView(int position, View convertView, ViewGroup
            parent) {
        return getView(position, convertView, parent);

    }
}
