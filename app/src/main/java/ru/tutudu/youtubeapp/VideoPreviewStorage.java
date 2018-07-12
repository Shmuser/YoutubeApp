package ru.tutudu.youtubeapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;

import okhttp3.HttpUrl;

/**
 * Created by vlad on 11.07.18.
 */

public class VideoPreviewStorage {

    private String nextToken;
    private HttpUrl.Builder request;
    private String videoIdsForQuery;
    private ArrayList<VideoPreview> data;
    private int count = 0;
    public int searchState = 0;


    // 0 - search, 1 - base request, 2 -trends
    public VideoPreviewStorage(int what) {
        request = HttpUrl.parse("https://www.googleapis.com/youtube/v3/videos").newBuilder()
                .addQueryParameter("part","snippet,statistics,contentDetails")
                .addQueryParameter("chart","mostPopular")
                .addQueryParameter("maxResults","10")
                .addQueryParameter("regionCode",MainActivity.getCountryCode())
                .addQueryParameter("key", YoutubeConfig.getApiKey());
        if (what == 1)
            request.addQueryParameter("videoCategoryId", String.valueOf(new Random().nextInt(2) + 1));
    }

    public VideoPreviewStorage(String searchText) {
        searchState = 1;
        request = HttpUrl.parse("https://www.googleapis.com/youtube/v3/search").newBuilder()
                .addQueryParameter("part","snippet")
                .addQueryParameter("maxResults","10")
                .addQueryParameter("type", "video")
                .addQueryParameter("q", searchText)
                .addQueryParameter("regionCode",MainActivity.getCountryCode())
                .addQueryParameter("key", YoutubeConfig.getApiKey());
     }




    void changeRequest() {
        if (searchState != 2) {
            if (count != 0) {
                if (count == 1) {
                    request.addQueryParameter("pageToken", nextToken);
                } else {
                    request.setQueryParameter("pageToken", nextToken);
                }
            }
            ++count;
        }
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
                    if (searchState == 1) {
                        getVideoIdsString(new JSONObject(json));
                    }
                    else {
                        data = parseVideoListFromResponse(new JSONObject(json));
                    }
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
                    if (searchState != 1) {
                        for (VideoPreview elm : data) {
                            elm.saveVideoPreview();
                        }
                    }
                    searchState = 0;
            }
        }
    }

    public ArrayList<VideoPreview> getData(int a, int b) {
        RequestYoutubeAPI requestYoutubeAPI = new RequestYoutubeAPI();
        try {
            if (searchState == 1) {
                requestYoutubeAPI.execute().get();
                searchState = 2;
                RequestYoutubeAPI secondRequestYoutubeAPI = new RequestYoutubeAPI();
                secondRequestYoutubeAPI.execute().get();
            }
            else {
                requestYoutubeAPI.execute().get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private ArrayList<VideoPreview> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<VideoPreview> mList = new ArrayList<>();
        try {
            if (searchState != 2) {
                nextToken = jsonObject.getString("nextPageToken");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("id")) {
                        if (json.has("kind")) {
                            if (json.getString("kind").equals("youtube#video")) {
                                VideoPreview youtubeObject = new VideoPreview(MainActivity.getContext());
                                String videoId = json.getString("id");
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String title = jsonSnippet.getString("title");
                                String previewURL = jsonSnippet.getJSONObject("thumbnails")
                                        .getJSONObject("medium")
                                        .getString("url");

                                String viewsCount = json.getJSONObject("statistics").getString("viewCount");
                                String duration = json.getJSONObject("contentDetails").getString("duration");

                                duration = duration.substring(2);
                                duration = toFormat(duration);

                                youtubeObject.setVideoId(videoId);
                                youtubeObject.setVideoTitle(title);
                                youtubeObject.setVideoPreview(previewURL);
                                youtubeObject.setVideoViews(Integer.valueOf(viewsCount));
                                youtubeObject.setVideoDuration(duration);

                                mList.add(youtubeObject);
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


    private void getVideoIdsString(JSONObject jsonObject) {
        ArrayList<String> videoIds = parseVideoListFromResponseForSearch(jsonObject);
        videoIdsForQuery = TextUtils.join(",", videoIds);
        request = HttpUrl.parse("https://www.googleapis.com/youtube/v3/videos").newBuilder()
                .addQueryParameter("part", "snippet,statistics,contentDetails")
                .addQueryParameter("maxresults", "10")
                .addQueryParameter("regionCode", MainActivity.getCountryCode())
                .addQueryParameter("key", YoutubeConfig.getApiKey())
                .addQueryParameter("id", videoIdsForQuery);
    }

    private ArrayList<String> parseVideoListFromResponseForSearch(JSONObject jsonObject) {
        ArrayList<String> videoIds = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    Log.e("myAPP", json.toString());
                    if (json.has("id")) {
                        String videoId = json.getJSONObject("id").getString("videoId");
                        videoIds.add(videoId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return videoIds;
    }







    private String toFormat(String time) {
        String formatTime = "";
        int ind = time.indexOf("H");
        if (ind != -1) {
            formatTime += time.substring(0, time.indexOf("H"));
        }
        time = time.substring(ind + 1);

        ind = time.indexOf("M");
        if (ind != -1) {
            String minutes = time.substring(0, ind);
            if (!formatTime.equals("")) {
                if (minutes.length() < 2) {
                    formatTime = formatTime + ":0" + minutes;
                }
                else {
                    formatTime = formatTime + ":" + minutes;
                }
            }
            else {
                formatTime = minutes;
            }
            time = time.substring(ind + 1);

        }
        else {
            if (!formatTime.equals("")) {
                formatTime += ":00";
            }
        }
        ind = time.indexOf("S");
        if (ind != -1) {
            String seconds = time.substring(0, ind);
            if (!formatTime.equals("")) {
                if (seconds.length() < 2) {
                    formatTime = formatTime + ":0" + seconds;
                }
                else {
                    formatTime = formatTime + ":" + seconds;
                }
            }
            else {
                formatTime = seconds;
            }
        }
        else {
            formatTime += ":00";
        }

        return formatTime;
    }


}
