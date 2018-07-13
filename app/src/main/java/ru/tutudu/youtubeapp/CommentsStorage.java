package ru.tutudu.youtubeapp;

import android.os.AsyncTask;
import android.util.Log;

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
import java.util.ArrayList;

import okhttp3.HttpUrl;

/**
 * Created by vlad on 13.07.18.
 */

public class CommentsStorage {

    private String nextToken;
    private HttpUrl.Builder request;
    private ArrayList<Comment> data;
    private int count = 0;

    public CommentsStorage(String videoId) {
        request = HttpUrl.parse("https://www.googleapis.com/youtube/v3/commentThreads").newBuilder()
                .addQueryParameter("part", "snippet")
                .addQueryParameter("maxResults", "10")
                .addQueryParameter("order", "relevance")
                .addQueryParameter("videoId", videoId)
                .addQueryParameter("key", YoutubeConfig.getApiKey());
    }


    void changeRequest() {
        if (count != 0) {
            if (count == 1) {
                request.addQueryParameter("pageToken", nextToken);
            } else {
                request.setQueryParameter("pageToken", nextToken);}
        }
        ++count;
    }


    private class RequestYoutubeAPI extends AsyncTask<Void, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            changeRequest();
            HttpGet httpGet = new HttpGet(request.toString());
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);
                try {
                    data = parseCommentsListFromResponse(new JSONObject(json));
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
            if (response != null) {
                for (Comment elm : data) {
                    elm.saveAvatar();
                }
            }
        }
    }


    public ArrayList<Comment> getData(int a, int b) {
        RequestYoutubeAPI requestYoutubeAPI = new RequestYoutubeAPI();
        try {
            requestYoutubeAPI.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    private ArrayList<Comment> parseCommentsListFromResponse(JSONObject jsonObject) {
        ArrayList<Comment> mList = new ArrayList<>();
        try {
            nextToken = jsonObject.getString("nextPageToken");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    Log.e("MYapp", json.toString());
                    if (json.has("id")) {
                        if (json.has("kind")) {
                            if (json.getString("kind").equals("youtube#commentThread")) {
                                Comment comment = new Comment(VideoPlayerActivity.getContext());
                                JSONObject jsonSnippet = json.getJSONObject("snippet")
                                        .getJSONObject("topLevelComment")
                                        .getJSONObject("snippet");
                                String userName = jsonSnippet.getString("authorDisplayName");
                                String avatarURL = jsonSnippet.getString("authorProfileImageUrl");
                                String userComment = jsonSnippet.getString("textOriginal");
                                String time = jsonSnippet.getString("publishedAt");
                                String likes = jsonSnippet.getString("likeCount");
                                time = formatTime(time);

                                comment.setComment(userComment);
                                comment.setUserName(userName);
                                comment.setLikes(likes);
                                comment.setCommentDate(time);
                                comment.setUserAvatar(avatarURL);

                                mList.add(comment);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mList;
    }

    String formatTime(String time) {
        time = time.substring(0,16);
        time = time.replaceAll("-", ".");
        time = time.replace("T", "   ");
        return time;
    }


}



