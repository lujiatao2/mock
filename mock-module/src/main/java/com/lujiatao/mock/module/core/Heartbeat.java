package com.lujiatao.mock.module.core;

import com.alibaba.jvm.sandbox.api.resource.ConfigInfo;
import com.alibaba.jvm.sandbox.api.resource.ModuleManager;
import com.lujiatao.mock.module.constant.HttpMethod;
import com.lujiatao.mock.module.util.HttpUtil;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lujiatao.mock.module.constant.CommonConstant.MOCK_MODULE_ID;
import static com.lujiatao.mock.module.constant.CommonConstant.MOCK_MODULE_VERSION;
import static com.lujiatao.mock.module.util.CommonUtil.getJsonValue;

/**
 * 心跳
 *
 * @author 卢家涛
 */
public class Heartbeat {

    private static final Logger logger = LoggerFactory.getLogger(Heartbeat.class);

    private final ConfigInfo configInfo;

    private final ModuleManager moduleManager;

    private final AtomicBoolean init = new AtomicBoolean(false);

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("mock-module-heartbeat-%d").daemon(true).build());

    public Heartbeat(ConfigInfo configInfo, ModuleManager moduleManager) {
        this.configInfo = configInfo;
        this.moduleManager = moduleManager;
    }

    /**
     * 启动心跳
     */
    public void start() {
        if (init.compareAndSet(false, true)) {
            executorService.scheduleAtFixedRate(() -> {
                try {
                    String appEnv = MockApp.instance().getAppEnv();
                    String appName = MockApp.instance().getAppName();
                    String ip = MockApp.instance().getIp();
                    int port = configInfo.getServerAddress().getPort();
                    String mockBeUrl = MockApp.instance().getMockBeUrl();
                    try (HttpUtil httpClient = new HttpUtil.HttpUtilBuilder(mockBeUrl)
                            .path(String.format("/mock-app/%s/%s/%s/%d", appEnv, appName, ip, port))
                            .build()) {
                        String response = httpClient.execute().getResponseAsString();
                        int id = getJsonValue(response, "$.data");
                        String data = "{\n" +
                                "\t\"id\": " + id + ",\n" +
                                "\t\"appEnv\": \"" + MockApp.instance().getAppEnv() + "\",\n" +
                                "\t\"appName\": \"" + MockApp.instance().getAppName() + "\",\n" +
                                "\t\"ip\": \"" + MockApp.instance().getIp() + "\",\n" +
                                "\t\"port\": " + configInfo.getServerAddress().getPort() + ",\n" +
                                "\t\"version\": \"" + MOCK_MODULE_VERSION + "\",\n" +
                                "\t\"isEnable\": " + moduleManager.isActivated(MOCK_MODULE_ID) + "\n" +
                                "}";
                        httpClient.setMethod(id == 0 ? HttpMethod.POST : HttpMethod.PUT)
                                .setPath("/mock-app")
                                .setJsonParams(data);
                        response = httpClient.execute().getResponseAsString();
                        boolean success = getJsonValue(response, "$.success");
                        if (success) {
                            logger.info("上报数据成功：\nMock控制台后端的URL：{}\n上报的数据：{}", mockBeUrl, data);
                        } else {
                            throw new RuntimeException(String.valueOf(getJsonValue(response, "$.message")));
                        }
                    }
                } catch (Exception e) {
                    logger.error("上报数据失败：{}", e.getMessage());
                }
            }, 0, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * 停止心跳
     */
    public void stop() {
        if (init.compareAndSet(true, false)) {
            executorService.shutdown();
        }
    }

}
