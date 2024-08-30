package com.cwc.exception;


import com.cwc.model.JudgeInfo;
import lombok.Getter;

/**
 * 自定义异常类
 */
@Getter
public class ExecuteException extends RuntimeException {

    private final JudgeInfo judgeInfo;

    public ExecuteException(JudgeInfo judgeInfo, String message) {
        super(message);
        this.judgeInfo = judgeInfo;
    }
}
