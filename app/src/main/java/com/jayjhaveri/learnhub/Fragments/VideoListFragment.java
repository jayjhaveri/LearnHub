package com.jayjhaveri.learnhub.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.BookmarkActivity;
import com.jayjhaveri.learnhub.EditVideoActivity;
import com.jayjhaveri.learnhub.LikeVideosActivity;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.SearchResultsActivity;
import com.jayjhaveri.learnhub.UserVideosActivity;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.model.VideoDetail;
import com.jayjhaveri.learnhub.viewholder.VideoViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jayjhaveri.learnhub.BaseActivity.ACTION_DATA_UPDATED;

/**
 * Created by ADMIN-PC on 21-03-2017.
 */

public abstract class VideoListFragment extends Fragment {

    public static final String EXTRA_VIDEO_UID = "video_uid";
    public static final String EXTRA_VIDEO_KEY = "userVideo";
    public static final String EXTRA_SCROLL = "scroll";
    public static final String EXTRA_VIDEO_CATEGORY = "category";

    public static final String ACTION_UPDATED = "com.jayjhaveri.learnhub.ACTION_DATA_UPDATED";
    private static final String TAG = "VideoListFragment";
    // [END define_database_reference]
    // [START define_database_reference]
    protected DatabaseReference databaseReference;
    protected FirebaseStorage firebaseStorage;
    protected LinearLayoutManager linearLayoutManager;
    protected FirebaseIndexRecyclerAdapter<VideoDetail, VideoViewHolder> likedAndBookmarkAdapter;
    protected boolean isBookMarkActivity = false;
    Activity activity;
    @BindView(R.id.rv_video_list)
    RecyclerView rv_video_list;
    @BindView(R.id.tv_empty_recyclerView)
    TextView tv_empty;
    @BindView(R.id.pb_video_list)
    ProgressBar pb_video_list;
    //Ternary operator
    String likes = null;
    private StorageReference imageReference;
    private StorageReference mProfileImageReference;
    private FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder> firebaseRecyclerAdapter;

    //Check isUserVideoActivity
    private boolean isUserVideoActivity = false;
    private boolean isLikedVideoActivity = false;
    private boolean isSearchActivity = false;

    private String search;
    private DatabaseReference sortRef;

    public VideoListFragment() {
    }

    public static void updateWidgets(Context context) {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_video, container, false);
        ButterKnife.bind(this, rootView);
        //Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();
        rv_video_list.setHasFixedSize(true);

        firebaseStorage = FirebaseStorage.getInstance();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isBookMarkActivity || isLikedVideoActivity) {
            if (isBookMarkActivity) {
                likes = "bookmarks";
            } else {
                likes = "likes";
            }
            sortRef = databaseReference.child("users").
                    child(getUid()).child(likes);
            checkEmptyQuery(sortRef);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        linearLayoutManager = new LinearLayoutManager(getActivity());


        rv_video_list.setLayoutManager(linearLayoutManager);
//        rv_video_list.scrollToPosition(4);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(databaseReference);

        if (isNetworkAvailable()) {
            if (isLikedVideoActivity || isBookMarkActivity) {

                final DatabaseReference videosRef = databaseReference.child("videos");
                linearLayoutManager.setReverseLayout(false);
                linearLayoutManager.setStackFromEnd(false);
                if (isBookMarkActivity) {
                    likes = "bookmarks";
                } else {
                    likes = "likes";
                }
                Query sortRef = databaseReference.child("users").
                        child(getUid()).child(likes);

                likedAndBookmarkAdapter = new FirebaseIndexRecyclerAdapter<VideoDetail, VideoViewHolder>(VideoDetail.class,
                        R.layout.list_video,
                        VideoViewHolder.class,
                        sortRef,
                        videosRef) {

                    @Override
                    protected void onDataChanged() {
                        int i = getItemCount();
                        super.onDataChanged();
                    }


                    @Override
                    protected void onCancelled(DatabaseError error) {
                        Log.d("DatabaseError", "" + error);
                        super.onCancelled(error);
                    }

                    @Override
                    protected void populateViewHolder(final VideoViewHolder viewHolder, final VideoDetail model, int position) {
                        DatabaseReference videoRef = getRef(position);

                        int i = getItemCount();
                        imageReference = firebaseStorage.getReferenceFromUrl(model.imageUrl);
                        final String videoKey = videoRef.getKey();

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startVideoDetailActivity(videoKey, model, viewHolder.iv_video_image);
                            }
                        });

                        viewHolder.bt_item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startVideoDetailActivity(videoKey, model, viewHolder.iv_video_image);
                            }
                        });


                        viewHolder.bindToPost(getActivity(), model, imageReference);
                    }

                };
                rv_video_list.setAdapter(likedAndBookmarkAdapter);


            } else {
                checkEmptyQuery(postsQuery);
                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<VideoDetail, VideoViewHolder>(VideoDetail.class,
                        R.layout.list_video,
                        VideoViewHolder.class,
                        postsQuery) {


                    @Override
                    public int getItemViewType(int position) {
                        return super.getItemViewType(position);
                    }

                    @Override
                    protected void onDataChanged() {
                        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
                        updateWidgets(activity);
                        activity.sendBroadcast(dataUpdatedIntent);
                        linearLayoutManager.setReverseLayout(true);
                        linearLayoutManager.setStackFromEnd(true);
                        super.onDataChanged();
                    }

                    @Override
                    protected void populateViewHolder(final VideoViewHolder viewHolder, final VideoDetail model, final int position) {
                        DatabaseReference videoRef = getRef(position);

                        imageReference = firebaseStorage.getReferenceFromUrl(model.imageUrl);
                        final String videoKey = videoRef.getKey();


                        if (isUserVideoActivity && getUid() != null) {
                            viewHolder.bt_item.setVisibility(View.GONE);
                            if (getUid().equals(model.uid)) {
                                viewHolder.iv_popUp_menu.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        PopupMenu popup = new PopupMenu(getActivity(), view);
                                        MenuInflater inflater = popup.getMenuInflater();
                                        inflater.inflate(R.menu.menu_comment_option, popup.getMenu());
                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                switch (item.getItemId()) {
                                                    case R.id.edit:
                                                        edit(videoKey, model.category);
                                                        return true;
                                                    case R.id.delete:
                                                        DialogFragment newFragment = AlertDialogFragment.newInstance(
                                                                videoKey, model);
                                                        newFragment.show(getFragmentManager(), "dialog");
                                                        return true;
                                                    default:
                                                        return false;
                                                }
                                            }
                                        });
                                        popup.show();
                                    }
                                });
                            }
                        } else {
                            viewHolder.iv_popUp_menu.setVisibility(View.GONE);
                            viewHolder.bt_item.setVisibility(View.VISIBLE);
                        }


                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startVideoDetailActivity(videoKey, model, viewHolder.iv_video_image);
                            }
                        });

                        viewHolder.bt_item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startVideoDetailActivity(videoKey, model, viewHolder.iv_video_image);
                            }
                        });


                        viewHolder.bindToPost(getActivity(), model, imageReference);

                        if (getItemCount() == 0) {
                            tv_empty.setVisibility(View.VISIBLE);
                        } else {
                            tv_empty.setVisibility(View.GONE);
                        }
                    }
                };
                rv_video_list.setAdapter(firebaseRecyclerAdapter);
            }
        } else {
            tv_empty.setText(R.string.no_internet);
            tv_empty.setVisibility(View.VISIBLE);
            pb_video_list.setVisibility(View.GONE);
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_SCROLL)) {
                    int i = savedInstanceState.getInt(EXTRA_SCROLL);
                    rv_video_list.scrollToPosition(i);
                }
            }
        }, 200);

    }

    protected void checkEmptyQuery(Query postsQuery) {
        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    tv_empty.setVisibility(View.VISIBLE);
                    pb_video_list.setVisibility(View.GONE);
                } else {
                    tv_empty.setVisibility(View.GONE);
                    pb_video_list.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void startVideoDetailActivity(String videoKey, VideoDetail model, View imageView) {
        Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
        intent.putExtra(VideoDetailActivity.EXTRA_POST_KEY, videoKey);
//        intent.putExtra(VideoDetailActivity.EXTRA_VIDEO_DETAIL, model);

        Pair<View, String> image = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image = Pair.create(imageView, imageView.getTransitionName());
        }


            startActivity(intent);

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isLikedVideoActivity || isBookMarkActivity) {
//            likedAndBookmarkAdapter.clearAdapter();
        }
    }

    @Override
    public void onAttach(Context context) {

        activity = (Activity) context;
        if (context instanceof UserVideosActivity) {
            isUserVideoActivity = true;
        } else if (context instanceof BookmarkActivity) {
            isBookMarkActivity = true;
        } else if (context instanceof LikeVideosActivity) {
            isLikedVideoActivity = true;
        } else if (context instanceof SearchResultsActivity) {
            search = ((SearchResultsActivity) context).searchQuery;
            isSearchActivity = true;
        }
        super.onAttach(context);
    }

    private void edit(String videoKey, String category) {
        Intent intent = new Intent(getActivity(), EditVideoActivity.class);
        intent.putExtra(EXTRA_VIDEO_KEY, videoKey);
        intent.putExtra(EXTRA_VIDEO_CATEGORY, category);
        getActivity().startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (linearLayoutManager != null) {

            int lastFirstVisiblePosition = ((LinearLayoutManager) rv_video_list.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
//        outState.putParcelable("scroll", rv_video_list.getLayoutManager().onSaveInstanceState());
            outState.putInt(EXTRA_SCROLL, lastFirstVisiblePosition);
        }
    }


}
