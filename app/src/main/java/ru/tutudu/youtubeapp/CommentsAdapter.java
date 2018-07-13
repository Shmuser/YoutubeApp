package ru.tutudu.youtubeapp;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

/**
 * Created by vlad on 13.07.18.
 */

public class CommentsAdapter extends PagedListAdapter<Comment, CommentsAdapter.CommentsViewHolder> {


    CommentsAdapter(@NonNull DiffUtil.ItemCallback<Comment> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
        CommentsViewHolder holder = new CommentsViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        SparseBooleanArray mTogglePos = new SparseBooleanArray();
        holder.bind(getItem(position), mTogglePos, position);
    }

    static class CommentsViewHolder extends RecyclerView.ViewHolder {

        private ImageView userAvatar;
        private TextView userName;
        private TextView commentDate;
        private TextView likes;
        private ExpandableTextView comment;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userPicVP);
            userName = itemView.findViewById(R.id.userNameVP);
            commentDate = itemView.findViewById(R.id.commentDate);
            likes = itemView.findViewById(R.id.likesCommentCount);
            comment = itemView.findViewById(R.id.expandCommentView);
        }

        public void bind(Comment elem, SparseBooleanArray array, int pos) {
            if (elem != null) {
                Picasso.with(MainActivity.getContext()).load(elem.getUserAvatar()).into(userAvatar);
                userName.setText(elem.getUserName());
                commentDate.setText(elem.getCommentDate());
                likes.setText(elem.getLikes());
                comment.setText(elem.getComment(), array, pos);
            }
        }
    }
}
