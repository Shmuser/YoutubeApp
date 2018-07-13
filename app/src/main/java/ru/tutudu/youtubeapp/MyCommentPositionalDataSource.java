package ru.tutudu.youtubeapp;

import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

/**
 * Created by vlad on 11.07.18.
 */

class MyCommentPositionalDataSource extends PositionalDataSource<Comment> {

    private final CommentsStorage employeeStorage;

    public MyCommentPositionalDataSource(CommentsStorage employeeStorage) {
        this.employeeStorage = employeeStorage;
    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<Comment> callback) {
        Log.d("myAPPe", "loadInitial, requestedStartPosition = " + params.requestedStartPosition +
                ", requestedLoadSize = " + params.requestedLoadSize);
        List<Comment> result = employeeStorage.getData(params.requestedStartPosition, params.requestedLoadSize);

        callback.onResult(result, 0);
    }


    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<Comment> callback) {
        Log.d("myAPPe", "loadRange, startPosition = " + params.startPosition + ", loadSize = " + params.loadSize);
        List<Comment> result = employeeStorage.getData(params.startPosition, params.loadSize);
        callback.onResult(result);
    }

}