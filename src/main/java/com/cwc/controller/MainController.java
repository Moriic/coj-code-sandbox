package com.cwc.controller;

import com.cwc.codesandbox.JavaDockerCodeSandbox;
import com.cwc.model.ExecuteCodeRequest;
import com.cwc.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController("/")
public class MainController {
    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;
    // private JavaNativeCodeSandbox javaNativeCodeSandbox;

    public static final String AUTH_REQUEST_HEADER = "auth";

    public static final String AUTH_REQUEST_SECRET = "secretKey";

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                           HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader(AUTH_REQUEST_HEADER);
        if (!header.equals(AUTH_REQUEST_SECRET)) {
            response.setStatus(403);
            return null;
        }

        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        log.info("receive request: {}", executeCodeRequest);
        // return javaNativeCodeSandbox.executeCode(executeCodeRequest);
        return javaDockerCodeSandbox.executeCode(executeCodeRequest);
    }

}
