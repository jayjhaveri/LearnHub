package com.jayjhaveri.learnhub.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.model.VideoDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ADMIN-PC on 21-03-2017.
 */

public class VideoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.bt_item)
    public Button bt_item;
    @BindView(R.id.iv_video_image)
    ImageView iv_video_image;
    @BindView(R.id.iv_profile_image)
    CircleImageView iv_profile_image;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_author)
    TextView tv_author;
    @BindView(R.id.tv_upload_time)
    TextView tv_upload_time;


    public VideoViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this,itemView);
    }

    public void bindToPost(Context context, VideoDetail videoDetail, StorageReference image) {
        tv_title.setText(videoDetail.title);
        tv_author.setText(videoDetail.author);
//        tv.setText(String.valueOf(post.starCount));

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(image)
                .placeholder(R.drawable.dummy_thumbnail)
                .into(iv_video_image);

        Glide.with(context)
                .load(videoDetail.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(iv_profile_image);
    }
}
