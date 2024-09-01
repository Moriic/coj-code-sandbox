package com.cwc.codesandbox.resultcallback;

import com.cwc.model.ExecuteMessage;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyExecStartResultCallback extends ExecStartResultCallback {
    private final boolean[] timeout;

    private final ExecuteMessage executeMessage;

    public MyExecStartResultCallback(boolean[] timeout, ExecuteMessage executeMessage) {
        this.timeout = timeout;
        this.executeMessage = executeMessage;
    }

    @Override
    public void onComplete() {
        timeout[0] = false;
        super.onComplete();
    }

    @Override
    public void onNext(Frame item) {
        StreamType streamType = item.getStreamType();
        // 处理错误流
        if (streamType.equals(StreamType.STDERR)) {
            log.error("onNext error : {}", new String(item.getPayload()));
            executeMessage.setErrorMessage(new String(item.getPayload()));
        } else {
            log.info("onNext success : {}", new String(item.getPayload()));
            executeMessage.setMessage(new String(item.getPayload()));
        }

    }
}