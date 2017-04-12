package com.jayjhaveri.learnhub.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.UserVideosActivity;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.model.VideoDetail;

/**
 * Created by ADMIN-PC on 09-04-2017.
 */

public class AlertDialogFragment extends DialogFragment {

    static final String EXTRA_TITLE = "title";

    public static AlertDialogFragment newInstance(String videoKey, VideoDetail videoDetail) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TITLE, R.string.alert_dialog_two_buttons_title);
        args.putString(VideoDetailActivity.EXTRA_POST_KEY, videoKey);
        args.putSerializable(VideoDetailActivity.EXTRA_VIDEO_DETAIL, videoDetail);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt(EXTRA_TITLE);
        final String videoKey = getArguments().getString(VideoDetailActivity.EXTRA_POST_KEY);
        final VideoDetail videoDetail = (VideoDetail) getArguments().getSerializable(VideoDetailActivity.EXTRA_VIDEO_DETAIL);

        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((UserVideosActivity) getActivity()).doPositiveClick(videoKey, videoDetail);
                            }
                        }
                )
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((UserVideosActivity) getActivity()).doNegativeClick(videoKey);
                            }
                        }
                )
                .create();
    }
}
