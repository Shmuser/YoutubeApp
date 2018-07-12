package ru.tutudu.youtubeapp;

import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView mListView;
    private ImageView searchPic;
    private ImageView userPic;
    private VideoPreviewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    final OkHttpClient client = new OkHttpClient();
   // private HttpUrl.Builder baseRequest;
    private List<VideoPreview> newElems;
   // private String nextToken;
    private static Context mContext;
    final private Random randomVideoId = new Random();


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


    public static Context getContext() {
        return mContext;
    }

    private void updateList() {
        for(VideoPreview elm : newElems) {
          //  mAdapter.add(elm);
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
        mContext = getApplicationContext();
        mLayoutManager = new LinearLayoutManager(this);
        mListView.setLayoutManager(mLayoutManager);

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso().toUpperCase();

        MyPositionalDataSource dataSource = new MyPositionalDataSource(new VideoPreviewStorage(countryCode));

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .setInitialLoadSizeHint(10)
                .build();

        Log.e("MYAPP2", "hier");
        PagedList pagedList = new PagedList.Builder(dataSource, config)
                .setNotifyExecutor(new MainThreadExecutor())
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build();


        mAdapter = new VideoPreviewAdapter(new DiffUtil.ItemCallback<VideoPreview>() {
            @Override
            public boolean areItemsTheSame(VideoPreview oldItem, VideoPreview newItem) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(VideoPreview oldItem, VideoPreview newItem) {
                return false;
            }
        });
        mAdapter.submitList(pagedList);
        mAdapter.setContext(getApplicationContext());

        mListView.setAdapter(mAdapter);


        Log.e("myAPP", countryCode.toUpperCase());

        GoogleAccount.init(getApplicationContext());

        userPic.setOnClickListener(this);

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
}
