package com.cwc.codesandbox.resultcallback;

import com.cwc.model.ExecuteMessage;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

@Slf4j
public class MyResultCallback implements ResultCallback<Statistics> {
    private final ExecuteMessage executeMessage;

    public MyResultCallback(ExecuteMessage executeMessage) {
        this.executeMessage = executeMessage;
        executeMessage.setMemory(0L);
    }

    @Override
    public void onStart(Closeable closeable) {

    }

    /**
     * 记录下使用内存的最大值
     */
    @Override
    public void onNext(Statistics statistics) {
        Long usage = statistics.getMemoryStats().getUsage();
        if (usage != null) {
            executeMessage.setMemory(Math.max(executeMessage.getMemory(), usage));
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
