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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView mListView;
    private ImageView searchPic;
    private ImageView userPic;
    private ImageView trendsBtn;
    private ImageView homeBtn;
    private EditText searchText;
    private VideoPreviewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private PagedList.Config config;
    private static Context mContext;
    private static String countryCode;


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
            case R.id.trendsPicBVMA:
                if (trendsBtn.isSelected())
                    break;
                trendsBtn.setSelected(true);
                homeBtn.setSelected(false);
                changeReguest(2, null);
                break;
            case R.id.homePicBVMA:
                if (homeBtn.isSelected())
                    break;
                trendsBtn.setSelected(false);
                homeBtn.setSelected(true);
                changeReguest(1, null);
                break;

            case R.id.searchPicInTVMA:
                searchText.setText("");
            default:
                break;
        }
    }

    private void changeReguest(int value, String searchText) {
        MyPositionalDataSource dataSource;
        if (searchText == null) {
            dataSource = new MyPositionalDataSource(new VideoPreviewStorage(value));
        }
        else {
            dataSource = new MyPositionalDataSource(new VideoPreviewStorage(searchText));
        }
        PagedList pagedList = new PagedList.Builder(dataSource, config)
                .setNotifyExecutor(new MainThreadExecutor())
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build();

        mAdapter.submitList(pagedList);
    }


    public static String getCountryCode() {
        return countryCode;
    }


    public static Context getContext() {
        return mContext;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.listViewMA);
        searchPic = findViewById(R.id.searchPicInTVMA);
        userPic = findViewById(R.id.userPicTVMA);
        trendsBtn = findViewById(R.id.trendsPicBVMA);
        homeBtn = findViewById(R.id.homePicBVMA);
        searchText = findViewById(R.id.searchTextMA);
        trendsBtn.setOnClickListener(this);
        homeBtn.setOnClickListener(this);
        searchPic.setOnClickListener(this);
        homeBtn.setSelected(true);
        trendsBtn.setSelected(false);
        mContext = getApplicationContext();
        mLayoutManager = new LinearLayoutManager(this);
        mListView.setLayoutManager(mLayoutManager);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((i == EditorInfo.IME_ACTION_DONE) || (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (keyEvent.getAction() == KeyEvent.ACTION_DOWN )){
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(textView.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    changeReguest(0, searchText.getText().toString());
                    return true;
                }
                else{
                    return false;
                }
            }
        });

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        countryCode = tm.getSimCountryIso().toUpperCase();

        MyPositionalDataSource dataSource = new MyPositionalDataSource(new VideoPreviewStorage(1));

        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .setInitialLoadSizeHint(10)
                .build();

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
                userPic.setImageResource(R.drawable.account_box);
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
            userPic.setImageResource(R.drawable.incognito);
        }
        else {
            Picasso.with(this).load(mGoogleSignInAccount.getPhotoUrl()).into(userPic);
        }

    }
}
