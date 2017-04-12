package com.jayjhaveri.learnhub.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.model.VideoDetail;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ADMIN-PC on 21-03-2017.
 */

public class VideoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.bt_item)
    public Button bt_item;
    @BindView(R.id.iv_popUp_menu)
    public ImageView iv_popUp_menu;
    @BindView(R.id.iv_video_image)
    public ImageView iv_video_image;
    Context context;
    @BindView(R.id.iv_profile_image)
    CircleImageView iv_profile_image;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_author)
    TextView tv_author;
    @BindView(R.id.tv_upload_time)
    TextView tv_upload_time;
    @BindView(R.id.tv_views)
    TextView tv_views;
    @BindView(R.id.tv_duration)
    TextView tv_duration;

    FirebaseAuth auth;


    public VideoViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        auth = FirebaseAuth.getInstance();
    }

    public void bindToPost(final Context context, final VideoDetail videoDetail, StorageReference image) {
        tv_title.setText(videoDetail.title);
        tv_author.setText(videoDetail.author + " • ");
//        tv.setText(String.valueOf(post.starCount));
        this.context = context;
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(image)
                .placeholder(R.drawable.dummy_thumbnail)
                .into(iv_video_image);

        iv_video_image.setContentDescription(videoDetail.title);

        Glide.with(context)
                .load(videoDetail.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(iv_profile_image);

        PrettyTime p = new PrettyTime();
        if (videoDetail.getTimestamp() != 0) {
            tv_upload_time.setText(p.format(new Date(videoDetail.getTimestamp())));
        }
        tv_views.setText(videoDetail.views + " views" + " • ");

        tv_duration.setText(videoDetail.duration);
    }

    private void delete(MenuItem item) {

    }


    private String getUid() {
        if (auth.getCurrentUser() != null) {

            return auth.getCurrentUser().getUid();
        }

        return null;
    }
}
