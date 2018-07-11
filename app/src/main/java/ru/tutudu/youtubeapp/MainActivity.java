package ru.tutudu.youtubeapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

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

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView mListView;
    private ImageView searchPic;
    private ImageView userPic;
    private VideoPreviewAdapter mAdapter;
    final OkHttpClient client = new OkHttpClient();
    private HttpUrl.Builder baseRequest;
    private List<VideoPreviewView> newElems;
    private String nextToken;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userPicTVMA:
                if (GoogleAccount.getAccount(getApplicationContext()) == null) {
                    signIn();
                }
                else {
                    Intent intentUserProfile = new Intent(this, UserProfileActivity.class);
                    startActivity(intentUserProfile);
                }
                break;

            default:
                break;
        }
    }


    private void updateList() {
        for(VideoPreviewView elm : newElems) {
            mAdapter.add(elm);
            Log.e("myAPP", "MY FRIENDZZZ");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.listViewMA);
        searchPic = findViewById(R.id.searchPicInTVMA);
        userPic = findViewById(R.id.userPicTVMA);

        //final VideoPreviewView videoPreview = new VideoPreviewView(getApplicationContext());

        final List<VideoPreviewView> fruits_list = new ArrayList<VideoPreviewView>();
       // fruits_list.add(videoPreview);

        mAdapter = new VideoPreviewAdapter(this, fruits_list);
        mListView.setAdapter(mAdapter);

        baseRequest = HttpUrl.parse("https://www.googleapis.com/youtube/v3/videos").newBuilder();
        baseRequest.addQueryParameter("part","snippet,statistics,contentDetails");
        baseRequest.addQueryParameter("chart","mostPopular");
        baseRequest.addQueryParameter("maxResults","5");

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso().toUpperCase();
        Log.e("myAPP", countryCode.toUpperCase());
        baseRequest.addQueryParameter("regionCode",countryCode); ///////////////////////////////////////////

        baseRequest.addQueryParameter("videoCategoryId","1");
        baseRequest.addQueryParameter("key", YoutubeConfig.getApiKey());

        Log.e("myAPP","UA " + baseRequest.toString());

        Log.e("myAPP","FE " + baseRequest.build().url());

        Log.e("MYAAAAAAPPP", baseRequest.toString());

        GoogleAccount.init(getApplicationContext());

        userPic.setOnClickListener(this);
        new RequestYoutubeAPI().execute();

    }

    private void signIn() {
        Task<GoogleSignInAccount> task = GoogleAccount.getClient().silentSignIn();
        if (task.isSuccessful()) {
            Toast.makeText(getApplicationContext(), "You are currently signed in", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent signInIntent = GoogleAccount.getClient().getSignInIntent();
            startActivityForResult(signInIntent, GoogleAccount.getCode());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleAccount.getCode()) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if(account.getPhotoUrl() != null){
                Picasso.with(this).load(account.getPhotoUrl()).into(userPic);
            }
            else {
                userPic.setImageResource(R.drawable.common_google_signin_btn_icon_dark);
            }
        } catch (ApiException e) {
            Toast.makeText(MainActivity.this, "Something gone wrong", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount mGoogleSignInAccount = GoogleAccount.getAccount(getApplicationContext());
        if (mGoogleSignInAccount == null) {
            userPic.setImageResource(R.mipmap.ic_launcher_round);
        }
        else {
            Picasso.with(this).load(mGoogleSignInAccount.getPhotoUrl()).into(userPic);
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
            HttpGet httpGet = new HttpGet(baseRequest.toString());
            Log.e("myAPP", baseRequest.toString());
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);

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
                    newElems = parseVideoListFromResponse(jsonObject);
                    Log.e("myAPP", "must added " + String.valueOf(newElems.size()));
                    updateList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public ArrayList<VideoPreviewView> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<VideoPreviewView> mList = new ArrayList<>();
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
                    //   JSONObject jsonID = json.getJSONObject("id");

                        if (json.has("kind")) {
                            if (json.getString("kind").equals("youtube#video")) {

                                VideoPreviewView youtubeObject = new VideoPreviewView(getApplicationContext());

                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String title = jsonSnippet.getString("title");
                                String previewURL = jsonSnippet.getJSONObject("thumbnails")
                                        .getJSONObject("medium")
                                        .getString("url");

                                String viewsCount = json.getJSONObject("statistics").getString("viewCount");
                                String duration = json.getJSONObject("contentDetails").getString("duration");

                                duration = duration.substring(2);
                                duration = toFormat(duration);

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
