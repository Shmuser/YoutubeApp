package ru.tutudu.youtubeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;

/**
 * Created by vlad on 09.07.18.
 */

public class VideoPreviewView extends RelativeLayout {

    private ImageView videoPreview;
    private TextView videoDuration;
    private TextView videoTitle;
    private TextView videoViews;
    private String previewURL;
    private String videiId;

    public VideoPreviewView(Context context) {
        super(context);
        initComponent();
    }

    private void initComponent() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.video_view,this);
        videoPreview = (ImageView) findViewById(R.id.videoPreview);
        videoDuration = (TextView) findViewById(R.id.videoDuration);
        videoTitle = (TextView) findViewById(R.id.videoTitle);
        videoViews = (TextView) findViewById(R.id.videoViews);
    }

    public void setVideoDuration(String length) {
        videoDuration.setText(length);
    }

    public void setVideoTitle(String name) {
        videoTitle.setText(name);

    }

    public void setVideoViews(int views) {
        String temp = String.valueOf(views) + " views";
        videoViews.setText(temp);
    }

    public void setVideoPreview(String url) {
        Picasso.with(getContext()).load(url).into(videoPreview);
        previewURL = url;
    }

    public void setVideiId(String id) {
        videiId = id;
    }

    public String getVideiId() {
        return videiId;
    }

    public String getVideoPreview() {
        return previewURL;
    }

    public String getVideoDuration() {
        return videoDuration.getText().toString();
    }

    public String getVideoTitle() {
        return videoTitle.getText().toString();
    }

    public String getVideoViews() {
        return videoViews.getText().toString();
    }
}
