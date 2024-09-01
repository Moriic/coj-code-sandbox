package com.cwc.codesandbox.resultcallback;

import com.cwc.model.ExecuteMessage;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyExecStartResultCallback extends ExecStartResultCallback {

    private final ExecuteMessage executeMessage;

    private final StringBuilder stderr = new StringBuilder();
    private final StringBuilder stdout = new StringBuilder();

    public MyExecStartResultCallback(ExecuteMessage executeMessage) {
        this.executeMessage = executeMessage;
    }

    @Override
    public void onComplete() {
        if (stderr.length() > 0) {
            executeMessage.setMessage(stderr.toString());
        } else {
            executeMessage.setMessage(stdout.toString());
        }
        log.info("onComplete: {}", executeMessage.getMessage());
        super.onComplete();
    }

    @Override
    public void onNext(Frame item) {
        StreamType streamType = item.getStreamType();
        String payload = new String(item.getPayload());
        // 处理错误流
        if (streamType.equals(StreamType.STDERR)) {
            log.error("onNext error : {}", payload);
            stderr.append(payload);
        } else if (streamType.equals(StreamType.STDOUT)) {
            log.info("onNext success : {}", payload);
            stdout.append(payload);
        }
    }
}