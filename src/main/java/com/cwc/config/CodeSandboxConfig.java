package com.cwc.config;

import com.cwc.codesandbox.CodeSandbox;
import com.cwc.codesandbox.JavaDockerCodeSandbox;
import com.cwc.codesandbox.JavaNativeCodeSandbox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeSandboxConfig {

    @Value("${codesandbox.type}")
    private String type;

    @Bean
    public CodeSandbox codeSandbox() {
        if (type.equals("docker")) {
            return new JavaDockerCodeSandbox();
        } else {
            return new JavaNativeCodeSandbox();
        }
    }
}
