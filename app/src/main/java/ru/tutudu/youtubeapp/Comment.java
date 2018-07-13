package ru.tutudu.youtubeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

/**
 * Created by vlad on 13.07.18.
 */

public class Comment extends RelativeLayout {

    private ImageView userAvatar;
    private TextView userName;
    private TextView commentDate;
    private TextView likes;
    private ExpandableTextView comment;
    private String avatarURL;

    public Comment(Context context) {
        super(context);
        initComponent();
    }

    public String getUserAvatar() {
        return avatarURL;
    }

    public void setUserAvatar(String url) {
        avatarURL = url;
    }

    public void saveAvatar() {
        Picasso.with(MainActivity.getContext()).load(avatarURL).into(userAvatar);
    }


    public String getUserName() {
        return userName.getText().toString();
    }

    public void setUserName(String name) {
        userName.setText(name);
    }

    public String getCommentDate() {
        return commentDate.getText().toString();
    }

    public void setCommentDate(String date) {
        commentDate.setText(date);
    }

    public String getLikes() {
        return likes.getText().toString();
    }

    public void setLikes(String likesCnt) {
        likes.setText(likesCnt);
    }

    public String getComment() {
        return comment.getText().toString();
    }

    public void setComment(String newComment) {
        comment.setText(newComment);
    }

    private void initComponent() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comment_view,this);
        userAvatar = (ImageView) findViewById(R.id.userPicVP);
        userName = (TextView) findViewById(R.id.userNameVP);
        commentDate = (TextView) findViewById(R.id.commentDate);
        comment = findViewById(R.id.expandCommentView);
        likes = (TextView) findViewById(R.id.likesCommentCount);
    }
}
