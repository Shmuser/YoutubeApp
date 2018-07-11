package ru.tutudu.youtubeapp;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView userPic, homePic, trendsPic;
    TextView userName, userEmail;
    Button likedVideoBtn, subscribtionsBtn, signOutBtn, myVideoBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userPic = (ImageView) findViewById(R.id.userPicPA);
        userName = (TextView) findViewById(R.id.userNamePA);
        userEmail = (TextView) findViewById(R.id.userEmailPA);
        signOutBtn = (Button) findViewById(R.id.signOutBtnPA);
        homePic = (ImageView)findViewById(R.id.homePicPA);
        signOutBtn.setOnClickListener(this);
        homePic.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount mGoogleSignInAccount = GoogleAccount.getAccount(getApplicationContext());
        userName.setText(mGoogleSignInAccount.getDisplayName());
        userEmail.setText(mGoogleSignInAccount.getEmail());
        Picasso.with(this).load(mGoogleSignInAccount.getPhotoUrl()).into(userPic);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signOutBtnPA:
                GoogleAccount.signOut(getApplicationContext());
                Intent intentMain = new Intent(this,MainActivity.class);
                startActivity(intentMain);
                break;

            case R.id.homePicPA:
           //     Intent intentMain = new Intent(this,MainActivity.class);
                startActivity(new Intent(this,MainActivity.class));
                break;

            default:
                break;
        }
    }
}
