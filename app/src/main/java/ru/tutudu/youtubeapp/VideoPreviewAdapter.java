package ru.tutudu.youtubeapp;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by vlad on 09.07.18.
 */


class VideoPreviewAdapter extends PagedListAdapter<VideoPreview, VideoPreviewAdapter.VideoPreviewViewHolder> {

    Context mContext;

    VideoPreviewAdapter(@NonNull DiffUtil.ItemCallback<VideoPreview> diffCallback) {
        super(diffCallback);
    }

    void setContext(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public VideoPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_view, parent, false);
        VideoPreviewViewHolder holder = new VideoPreviewViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoPreviewAdapter.VideoPreviewViewHolder holder, int position) {
        holder.bind(getItem(position));
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int pos) {

                Log.e("onClick", "2");
                Intent intentVideo = new Intent(mContext, VideoPlayerActivity.class);
                intentVideo.putExtra("videoId", getItem(pos).getVideoId());
                intentVideo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intentVideo);
            }
        });
    }



    static class VideoPreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView videoPreview;
        TextView videoDuration;
        TextView videoTitle;
        TextView videoViews;
        private ItemClickListener itemClickListener;


        void setItemClickListener(ItemClickListener listener) {
            itemClickListener = listener;
        }


        @Override
        public void onClick(View view) {
            Log.e("onClick", "1");
            itemClickListener.onClick(view, getAdapterPosition());
        }

        public VideoPreviewViewHolder(View view) {
            super(view);
            videoPreview = (ImageView) view.findViewById(R.id.videoPreview);
            videoDuration = (TextView) view.findViewById(R.id.videoDuration);
            videoTitle = (TextView) view.findViewById(R.id.videoTitle);
            videoViews = (TextView) view.findViewById(R.id.videoViews);
            view.setOnClickListener(this);
        }

        public void bind(VideoPreview elem) {
            if (elem != null) {
                videoDuration.setText(elem.getVideoDuration());
                videoTitle.setText(elem.getVideoTitle());
                videoViews.setText(elem.getVideoViews());
                Picasso.with(MainActivity.getContext()).load(elem.getVideoPreview()).into(videoPreview);
            }
        }
    }



}