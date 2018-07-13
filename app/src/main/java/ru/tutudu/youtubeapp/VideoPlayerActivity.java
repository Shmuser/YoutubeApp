package ru.tutudu.youtubeapp;

import android.arch.paging.PagedList;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;

public class VideoPlayerActivity extends YouTubeBaseActivity {

    YouTubePlayerView mYouTubePlayerView;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    ExpandableTextView expandableTextView;
    RecyclerView mCommentsView;
    CommentsAdapter commentsAdapter;
    static Context mContext;
    TextView likesView, dislikesView, viewsView;


    public static Context getContext() {
        return mContext;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        final String videoId = getIntent().getStringExtra("videoId");
        mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.videoPlayerView);
        expandableTextView = findViewById(R.id.expand_text_view);
        mCommentsView = findViewById(R.id.commentsView);
        likesView = findViewById(R.id.likesCount);
        viewsView = findViewById(R.id.viewsCount);
        dislikesView = findViewById(R.id.dislikesCount);
        expandableTextView.setText("desciption");
        mContext = getApplicationContext();

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(videoId);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
            }
        };

        mYouTubePlayerView.initialize(YoutubeConfig.getApiKey(), mOnInitializedListener);

        new RequestForVideoData().execute();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mCommentsView.setLayoutManager(mLayoutManager);

        MyCommentPositionalDataSource dataSource = new MyCommentPositionalDataSource(new CommentsStorage(videoId));

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(20)
                .setInitialLoadSizeHint(20)
                .build();

        PagedList pagedList = new PagedList.Builder(dataSource, config)
                .setNotifyExecutor(new MainThreadExecutor())
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build();

        commentsAdapter = new CommentsAdapter(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(Comment oldItem, Comment newItem) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(Comment oldItem, Comment newItem) {
                return false;
            }
        });
        commentsAdapter.submitList(pagedList);
        mCommentsView.setAdapter(commentsAdapter);
    }


    private class RequestForVideoData extends AsyncTask<Void, String, String> {
        HttpUrl.Builder request = HttpUrl.parse("https://www.googleapis.com/youtube/v3/videos").newBuilder()
                .addQueryParameter("part","snippet,statistics")
                .addQueryParameter("key", YoutubeConfig.getApiKey())
                .addQueryParameter("id", getIntent().getStringExtra("videoId"));

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(request.toString());
            Log.e("My app" , request.toString());
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);
                try {
                    fillVideoData(new JSONObject(json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return json;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
        }
    }


    private void fillVideoData(JSONObject jsonObject) {
        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    Log.e("My app", " huyap");
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("id")) {
                        if (json.has("kind")) {
                            if (json.getString("kind").equals("youtube#video")) {
                                String videoId = json.getString("id");
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String description = jsonSnippet.getString("description");
                                JSONObject jsonStat = json.getJSONObject("statistics");
                                String likeCount = jsonStat.getString("likeCount");
                                String disCount = jsonStat.getString("dislikeCount");
                                String views = jsonStat.getString("viewCount");

                                expandableTextView.setText(description);
                                likesView.setText(likeCount);
                                dislikesView.setText(disCount);
                                viewsView.setText(views + " views");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
