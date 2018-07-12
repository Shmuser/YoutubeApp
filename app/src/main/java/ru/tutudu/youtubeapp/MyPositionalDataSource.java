package ru.tutudu.youtubeapp;

import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

/**
 * Created by vlad on 11.07.18.
 */

class MyPositionalDataSource extends PositionalDataSource<VideoPreview> {

    private final VideoPreviewStorage employeeStorage;

    public MyPositionalDataSource(VideoPreviewStorage employeeStorage) {
        this.employeeStorage = employeeStorage;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<VideoPreview> callback) {
        Log.d("myAPPe", "loadInitial, requestedStartPosition = " + params.requestedStartPosition +
                ", requestedLoadSize = " + params.requestedLoadSize);
        List<VideoPreview> result = employeeStorage.getData(params.requestedStartPosition, params.requestedLoadSize);
        if (result == null)
            Log.d("myAPPe", "oops(");

        callback.onResult(result, 0);
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<VideoPreview> callback) {
        Log.d("myAPPe", "loadRange, startPosition = " + params.startPosition + ", loadSize = " + params.loadSize);
        List<VideoPreview> result = employeeStorage.getData(params.startPosition, params.loadSize);
        callback.onResult(result);
    }

}