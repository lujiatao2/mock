package com.lujiatao.mock.web.configuration;

import com.lujiatao.mock.api.MockAppService;
import com.lujiatao.mock.common.entity.MockApp;
import com.lujiatao.mock.web.util.MultiThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务配置
 *
 * @author 卢家涛
 */
@EnableScheduling
@Configuration
public class TaskConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TaskConfiguration.class);

    private final MockAppService mockAppService;

    @Autowired
    public TaskConfiguration(MockAppService mockAppService) {
        this.mockAppService = mockAppService;
    }

    /**
     * 心跳定时任务
     */
    @Scheduled(initialDelay = 0, fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    private void heartbeat() {
        List<MockApp> mockApps = mockAppService.getAll();
        Collection<Heartbeat> heartbeats = new ArrayList<>();
        for (MockApp mockApp : mockApps) {
            heartbeats.add(new Heartbeat(mockApp));
        }
        MultiThreadUtil<Void> multiThreadUtil = new MultiThreadUtil<>(heartbeats);
        int processorCount = Runtime.getRuntime().availableProcessors();
        multiThreadUtil.execute(processorCount);
    }

    private class Heartbeat implements Callable<Void> {

        private final MockApp mockApp;

        public Heartbeat(MockApp mockApp) {
            this.mockApp = mockApp;
        }

        @Override
        public Void call() throws Exception {
            boolean isOnline = mockAppService.checkHeartbeat(mockApp.getIp(), mockApp.getPort());
            if (isOnline) {
                mockApp.setEnable(true);
                logger.info("Mock应用（IP={}，端口={}）在线。", mockApp.getIp(), mockApp.getPort());
            } else {
                mockApp.setEnable(false);
                logger.warn("Mock应用（IP={}，端口={}）离线。", mockApp.getIp(), mockApp.getPort());
            }
            mockAppService.modify(mockApp);
            return null;
        }

    }

}
