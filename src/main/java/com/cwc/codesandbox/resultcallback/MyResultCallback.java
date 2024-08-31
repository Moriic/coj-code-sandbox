package com.cwc.codesandbox.resultcallback;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;

import java.io.Closeable;

public class MyResultCallback implements ResultCallback<Statistics> {
    private final long[] maxUse;

    public MyResultCallback(long[] maxUse) {
        this.maxUse = maxUse;
    }

    @Override
    public void onStart(Closeable closeable) {

    }

    /**
     * 记录下使用内存的最大值
     */
    @Override
    public void onNext(Statistics statistics) {
        Long maxUsage = statistics.getMemoryStats().getMaxUsage();
        if (maxUsage != null) {
            maxUse[0] = Math.max(maxUse[0], maxUsage);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void close() {

    }
}