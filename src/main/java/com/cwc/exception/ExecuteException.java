package com.cwc.exception;


import com.cwc.model.JudgeInfo;

/**
 * 自定义异常类
 */
public class ExecuteException extends RuntimeException {

    private final JudgeInfo judgeInfo;

    public ExecuteException(JudgeInfo judgeInfo, String message) {
        super(message);
        this.judgeInfo = judgeInfo;
    }

    public JudgeInfo getJudgeInfo() {
        return judgeInfo;
    }
}
