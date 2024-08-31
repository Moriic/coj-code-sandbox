package com.cwc.codesandbox.resultcallback;

import com.cwc.model.ExecuteMessage;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;

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
            executeMessage.setErrorMessage(new String(item.getPayload()));
        } else {
            executeMessage.setMessage(new String(item.getPayload()));
        }

    }
}