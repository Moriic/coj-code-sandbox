package com.cwc;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import com.cwc.exception.ExecuteException;
import com.cwc.exception.JudgeInfoMessageEnum;
import com.cwc.model.ExecuteCodeRequest;
import com.cwc.model.ExecuteCodeResponse;
import com.cwc.model.ExecuteMessage;
import com.cwc.model.JudgeInfo;
import com.cwc.utils.ProcessUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 20000;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //        System.setSecurityManager(new DefaultSecurityManager());
        //        System.setSecurityManager(new DenySecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 保存用户代码文件
        File userCodeFile = saveCodeToFile(code);

        // 编译代码，得到class文件
        compileFile(userCodeFile);

        // 执行代码,得到输出
        List<ExecuteMessage> executeMessages = runFile(userCodeFile, inputList);

        // 搜集输出
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessages);

        // 文件清理
        boolean b = deleteFile(userCodeFile);
        if (!b) {
            log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
        }

        return outputResponse;
    }

    /**
     * 代码保存为文件
     *
     * @param code
     * @return
     */
    public File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 用户代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 编译代码
     *
     * @param userCodeFile
     * @return
     */
    public void compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage("compile", compileProcess);
            if (executeMessage.getExitValue() != 0) {
                Long time = executeMessage.getTime();
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setTime(time);
                judgeInfo.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
                String message = executeMessage.getErrorMessage().replace(userCodeFile.getAbsolutePath(), "");
                throw new ExecuteException(judgeInfo, message);
            }
        } catch (IOException e) {
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue());
            throw new ExecuteException(judgeInfo, e.getMessage());
        }
    }

    /**
     * 执行文件，获得输出
     *
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            // -Xmx256m 最大堆空间大小 -Xms 初始堆空间大小
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Main", userCodeParentPath);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);

                // 写入输入数据
                try (BufferedWriter processInput = new BufferedWriter(
                    new OutputStreamWriter(runProcess.getOutputStream()))) {
                    processInput.write(inputArgs);
                    processInput.flush();
                }

                // 限制时间，创建新线程监控时间
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        if (runProcess.isAlive()) {
                            runProcess.destroy();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage("runCode ", runProcess);
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue());
                throw new ExecuteException(judgeInfo, e.getMessage());
            }
        }
        return executeMessageList;
    }

    /**
     * 整理搜集输出结果
     *
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        String errorMessage;
        List<String> outputList = new ArrayList<>();
        long maxTime = 0L;
        long maxMemory = 0L;
        for (ExecuteMessage executeMessage : executeMessageList) {
            errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            if (executeMessage.getTime() != null) {
                maxTime = Math.max(maxTime, executeMessage.getTime());
            }
            if (executeMessage.getMemory() != null) {
                maxMemory = Math.max(maxMemory, executeMessage.getMemory());
            }
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        // 正常运行
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        } else {
            executeCodeResponse.setStatus(3);
            executeCodeResponse.getJudgeInfo().setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue());
        }
        log.info(executeCodeResponse.toString());
        return executeCodeResponse;
    }

    /**
     * 文件清理
     *
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            if (del) {
                log.info("delete success !");
            } else {
                log.error("delete error !");
            }
            return del;
        }
        return true;
    }
}
