package com.jayjhaveri.learnhub.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.model.Category;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ADMIN-PC on 22-03-2017.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private CategoryClickListener categoryClickListener;

    public CategoryAdapter(List<Category> categories, Context context, CategoryClickListener categoryClickListener) {
        this.categoryList = categories;
        this.context = context;
        this.categoryClickListener = categoryClickListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.list_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.iv_category_image.setImageDrawable(new IconicsDrawable(context)
                .icon(category.getImageResource())
                .color(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .sizeDp(20)
        );
        holder.tv_category_name.setText(category.getCategoryName());

        holder.iv_category_image.setContentDescription(category.getCategoryName());
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public interface CategoryClickListener {
        public void onCategoryClick(String categoryName);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.iv_category_image)
        ImageView iv_category_image;

        @BindView(R.id.tv_category_name)
        TextView tv_category_name;

        public CategoryViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Category category = categoryList.get(getAdapterPosition());
            categoryClickListener.onCategoryClick(category.getCategoryName());
        }
    }
}
