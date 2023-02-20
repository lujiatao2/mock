package com.lujiatao.mock.module;

import com.alibaba.fastjson2.JSON;
import com.alibaba.jvm.sandbox.api.*;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.resource.ConfigInfo;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.alibaba.jvm.sandbox.api.resource.ModuleManager;
import com.lujiatao.mock.module.core.*;
import com.lujiatao.mock.module.util.CommonUtil;
import com.lujiatao.mock.module.util.HttpUtil;
import net.minidev.json.JSONArray;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import java.io.*;
import java.util.*;

import static com.lujiatao.mock.module.constant.CommonConstant.MOCK_MODULE_ID;
import static com.lujiatao.mock.module.constant.CommonConstant.MOCK_MODULE_VERSION;
import static com.lujiatao.mock.module.util.CommonUtil.getJsonValue;
import static com.lujiatao.mock.module.util.CommonUtil.readLastLines;

/**
 * Mock模块，遵循JVM Sandbox规范编写。
 *
 * @author 卢家涛
 */
@MetaInfServices(Module.class)
@Information(id = MOCK_MODULE_ID, version = MOCK_MODULE_VERSION, author = "卢家涛")
public class MockModule implements Module, ModuleLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(MockModule.class);

    @Resource
    private ConfigInfo configInfo;

    private String moduleInfo;

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    private Heartbeat heartbeat;

    @Resource
    private ModuleManager moduleManager;

    private final List<Integer> watchIds = new ArrayList<>();

    @Override
    public void onLoad() throws Throwable {
        Information.Mode mode = configInfo.getMode();
        moduleInfo = String.format("id = %s，version = %s，mode = %s。", MOCK_MODULE_ID, MOCK_MODULE_VERSION, mode);
        logger.info("开始加载模块：{}", moduleInfo);
        CommonUtil.configure(CommonUtil.getConfigPath() + "/mock-logback.xml");
    }

    @Override
    public void onUnload() throws Throwable {
        logger.info("开始卸载模块：{}", moduleInfo);
        heartbeat.stop();
        CommonUtil.unconfigure();
    }

    @Override
    public void onActive() throws Throwable {
        logger.info("激活模块成功：{}", moduleInfo);
    }

    @Override
    public void onFrozen() throws Throwable {
        logger.info("冻结模块成功：{}", moduleInfo);
    }

    @Override
    public void loadCompleted() {
        logger.info("加载模块成功：{}", moduleInfo);
        try {
            startWatch();
            heartbeat = new Heartbeat(configInfo, moduleManager);
            heartbeat.start();
            logger.info("初始化成功。");
        } catch (Exception e) {
            logger.error("初始化失败：{}", e.getMessage());
        }
    }

    /**
     * 开始监听
     */
    private void startWatch() {
        List<MockConfig> mockConfigs = getMockConfigs();
        for (MockConfig mockConfig : mockConfigs) {
            watchIds.add(new EventWatchBuilder(moduleEventWatcher)
                    .onClass(mockConfig.getMockClass())
                    .onBehavior(mockConfig.getMockMethod())
                    .onWatch(new MockAdviceListener(mockConfig))
                    .getWatchId()
            );
        }
    }

    /**
     * 停止监听
     */
    private void stopWatch() {
        for (int watchId : watchIds) {
            moduleEventWatcher.delete(watchId);
        }
        watchIds.clear();
    }

    /**
     * 获取Mock配置列表
     *
     * @return Mock配置列表
     */
    private List<MockConfig> getMockConfigs() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("appEnv", MockApp.instance().getAppEnv());
        queryParams.put("appName", MockApp.instance().getAppName());
        queryParams.put("mockClass", "");
        queryParams.put("mockMethod", "");
        queryParams.put("isEnable", "true");
        try (HttpUtil httpClient = new HttpUtil.HttpUtilBuilder(MockApp.instance().getMockBeUrl()).path("/mock-config/search")
                .queryParams(queryParams)
                .build()) {
            String response = httpClient.execute().getResponseAsString();
            boolean success = getJsonValue(response, "$.success");
            if (!success) {
                throw new RuntimeException("获取配置列表失败。");
            }
            JSONArray srcMockConfigs = getJsonValue(response, "$.data.targetData");
            List<MockConfig> mockConfigs = convertMockConfigs(srcMockConfigs);
            logger.info("获取配置列表成功。");
            return mockConfigs;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 转换Mock配置数据
     *
     * @param srcMockConfigs 原始Mock配置数据
     * @return 新Mock配置数据
     */
    private List<MockConfig> convertMockConfigs(JSONArray srcMockConfigs) {
        List<MockConfig> newMockConfigs = new ArrayList<>();
        for (Object srcMockConfig : srcMockConfigs) {
            MockConfig mockConfig = new MockConfig();
            LinkedHashMap<String, Object> tmp = (LinkedHashMap<String, Object>) srcMockConfig;
            mockConfig.setMockClass(String.valueOf(tmp.get("mockClass")));
            mockConfig.setMockMethod(String.valueOf(tmp.get("mockMethod")));
            ParameterRule[] parameterRules = JSON.parseArray(String.valueOf(tmp.get("parameterRules"))).toArray(ParameterRule.class);
            mockConfig.setParameterRules(parameterRules);
            mockConfig.setReturnOrThrowData(String.valueOf(tmp.get("returnOrThrowData")));
            newMockConfigs.add(mockConfig);
        }
        return newMockConfigs;
    }

    /**
     * 刷新配置
     *
     * @param printWriter 写入器
     */
    @Command("refresh-config")
    public void refreshConfig(PrintWriter printWriter) {
        try {
            stopWatch();
            startWatch();
            printWriter.print(true);
        } catch (Exception e) {
            logger.error("刷新配置失败：{}", e.getMessage());
            printWriter.print(false);
        }
    }

    /**
     * 查看日志
     *
     * @param printWriter 写入器
     */
    @Command("view-log")
    public void viewLog(PrintWriter printWriter) {
        try {
            // 只返回最后10000行，否则可能造成内存溢出。
            printWriter.print(readLastLines(System.getProperty("user.home") + "/logs/sandbox/mock.log", 10000));
        } catch (Exception e) {
            logger.error("查看日志失败：{}", e.getMessage());
            printWriter.print(false);
        }
    }

    /**
     * 校验心跳
     *
     * @param printWriter 写入器
     */
    @Command("check-heartbeat")
    public void checkHeartbeat(PrintWriter printWriter) {
        try {
            printWriter.print(true);
        } catch (Exception e) {
            logger.error("校验心跳失败：{}", e.getMessage());
            printWriter.print(false);
        }
    }

}
