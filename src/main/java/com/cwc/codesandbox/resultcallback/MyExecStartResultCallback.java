package com.cwc.codesandbox.resultcallback;

import com.cwc.model.ExecuteMessage;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MyExecStartResultCallback extends ExecStartResultCallback {

    private final ExecuteMessage executeMessage;

    public MyExecStartResultCallback(ExecuteMessage executeMessage) {
        this.executeMessage = executeMessage;
    }

    @Override
    public void onNext(Frame item) {
        StreamType streamType = item.getStreamType();
        // 处理错误流
        if (streamType.equals(StreamType.STDERR)) {
            log.error("onNext error : {}", new String(item.getPayload()));
            executeMessage.setErrorMessage(new String(item.getPayload()));
        } else {
            String output = new String(item.getPayload());
            if (StringUtils.isNotBlank(output)) {
                log.info("onNext success : {}", output);
                executeMessage.setMessage(output);
            }
        }

    }
}