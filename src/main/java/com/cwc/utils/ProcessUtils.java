package com.cwc.utils;

import com.cwc.model.ExecuteMessage;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 进程工具类
 */
@Slf4j
public class ProcessUtils {

    /**
     * 执行并获取输出
     *
     * @param runProcess
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(String opName, Process runProcess) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                log.info("{} success !", opName);
                InputStream inputStream = runProcess.getInputStream();
                StringBuilder sbf = new StringBuilder();

                if (inputStream.available() != 0) {
                    byte[] inputByte = new byte[inputStream.available()];
                    while (inputStream.read(inputByte) != -1) {
                        sbf.append(new String(inputByte));
                    }
                }
                executeMessage.setMessage(sbf.toString().replaceAll("\r\n", "\n"));
            } else {
                // 异常退出
                log.error("error, exitValue: {}", exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(runProcess.getInputStream(), "gbk"));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));

                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(
                    new InputStreamReader(runProcess.getErrorStream(), "gbk"));
                // 逐行读取
                List<String> errorOutputStrList = new ArrayList<>();
                // 逐行读取
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorOutputStrList, "\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;

    }
}
