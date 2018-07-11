package ru.tutudu.youtubeapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlad on 09.07.18.
 */

public class VideoPreviewAdapter extends ArrayAdapter<VideoPreviewView> {

    private Context mContext;
    private List<VideoPreviewView> videoPreviewViewList= new ArrayList<>();

    public VideoPreviewAdapter(Context context, List<VideoPreviewView> objects) {
        super(context, 0, objects);
        mContext = context;
        videoPreviewViewList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.video_view,parent,false);
        }
        VideoPreviewView cur = videoPreviewViewList.get(position);

        ImageView preview = (ImageView)listItem.findViewById(R.id.videoPreview);
        Picasso.with(mContext).load(cur.getVideoPreview()).into(preview);

        TextView title = (TextView)listItem.findViewById(R.id.videoTitle);
        title.setText(cur.getVideoTitle());

        TextView duration = (TextView)listItem.findViewById(R.id.videoDuration);
        duration.setText(cur.getVideoDuration());

        TextView views = (TextView)listItem.findViewById(R.id.videoViews);
        views.setText(cur.getVideoViews());

        return listItem;
    }
}