package com.cwc.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置 dockerClient
 */
@Slf4j
@Configuration
public class DockerConfig {
    @Value("${codesandbox.host}")
    private String sandboxHost;

    /**
     * 初始化 DockerClient实例
     */
    @Bean
    public DockerClient getDockerInstance() {
        // 设置配置
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(sandboxHost)
                .build();
        // 配置 httpClient
        DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(3000)
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient);
        log.info("connect dockerClient success !");
        // 拉取镜像
        String image = "openjdk:8-alpine";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                log.info("download images: {}", item.getStatus());
                super.onNext(item);
            }

            @Override
            public void onComplete() {
                log.info("download success!");
                super.onComplete();
            }
        };
        try {
            pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
        } catch (InterruptedException e) {
            log.info("download error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return dockerClient;
    }
}