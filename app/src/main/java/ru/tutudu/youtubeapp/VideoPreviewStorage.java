package ru.tutudu.youtubeapp;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
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
import java.util.Random;
import java.util.concurrent.ExecutionException;

import okhttp3.HttpUrl;

/**
 * Created by vlad on 11.07.18.
 */

public class VideoPreviewStorage {

    private String nextToken;
    private HttpUrl.Builder baseRequest;
    private ArrayList<VideoPreview> data;
    private int count = 0;
    public boolean flag = false;



    public VideoPreviewStorage(String countryCode) {
        baseRequest = HttpUrl.parse("https://www.googleapis.com/youtube/v3/videos").newBuilder();
        baseRequest.addQueryParameter("part","snippet,statistics,contentDetails");
        baseRequest.addQueryParameter("chart","mostPopular");
        baseRequest.addQueryParameter("maxResults","10");
        baseRequest.addQueryParameter("regionCode",countryCode);
        baseRequest.addQueryParameter("videoCategoryId", String.valueOf(new Random().nextInt(2)+1));
        baseRequest.addQueryParameter("key", YoutubeConfig.getApiKey());

    }


    void changeRequest() {
        if (count != 0) {
            if (count == 1) {
                baseRequest.addQueryParameter("pageToken", nextToken);
            }
            else {
                baseRequest.setQueryParameter("pageToken", nextToken);
            }
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
            HttpGet httpGet = new HttpGet(baseRequest.toString());
            Log.e("myAPP", "HMHM " + baseRequest.toString());
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);
                try {
                    data = parseVideoListFromResponse(new JSONObject(json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                flag = true;
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
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("myAPP", "onPost.. " +jsonObject.toString());
                  //  data = parseVideoListFromResponse(jsonObject);
                 //   flag = true;
                    Log.e("myAPP", "must added " + String.valueOf(data.size()));
                    //updateList();
                    for (VideoPreview elm : data) {
                        elm.saveVideoPreview();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<VideoPreview> getData(int a, int b) {
        RequestYoutubeAPI requestYoutubeAPI = new RequestYoutubeAPI();
        try {
            requestYoutubeAPI.execute().get();
        } catch (InterruptedException e) {
          //  Log.e("GOVNO", "1");
        } catch (ExecutionException e) {
           // Log.e("GOVNO", "2");
        }
         while (!flag) {

        }
        if (data == null)
            Log.e("MUAPP2", "HsIT");
        flag = false;

        return data;
    }

    private ArrayList<VideoPreview> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<VideoPreview> mList = new ArrayList<>();
        try {
            nextToken = jsonObject.getString("nextPageToken");
            Log.e("myAPP", "NEXT TOKEN " + nextToken);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    Log.e("myAPP", "obj");
                    JSONObject json = jsonArray.getJSONObject(i);
                    Log.e("myAPP", json.toString());
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
