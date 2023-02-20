package com.lujiatao.mock.module.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static com.jayway.jsonpath.JsonPath.read;

/**
 * 公共工具
 *
 * @author 卢家涛
 */
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static final Properties properties = new Properties();

    static {
        try {
            try (InputStream is = new FileInputStream(CommonUtil.getConfigPath() + "/mock.properties")) {
                properties.load(is);
            }
            logger.info("加载mock.properties配置文件成功。");
        } catch (Exception e) {
            logger.error("加载mock.properties配置文件失败：{}", e.getMessage());
        }
    }

    /**
     * 获取配置文件绝对路径
     *
     * @return 配置文件绝对路径
     */
    public static String getConfigPath() {
        try {
            ClassLoader classLoader = CommonUtil.class.getClassLoader();
            Class<? extends ClassLoader> aClass = classLoader.getClass();
            Field field = aClass.getDeclaredField("moduleJarFile");
            // 备份访问状态
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            File file = (File) field.get(classLoader);
            // 恢复访问状态
            field.setAccessible(accessible);
            String moduleFilePath = file.getParentFile().getAbsolutePath();
            String configPath = moduleFilePath + "/cfg";
            logger.info("获取配置文件绝对路径成功：{}", configPath);
            return configPath;
        } catch (Exception e) {
            throw new RuntimeException("获取配置文件绝对路径失败：" + e.getMessage());
        }
    }

    /**
     * 配置日志
     *
     * @param configFilePath 配置文件路径
     */
    public static void configure(String configFilePath) {
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            configurator.setContext(context);
            // 清除以前的配置，比如默认配置。
            context.reset();
            configurator.doConfigure(configFilePath);
            logger.info("配置日志成功。");
        } catch (Exception e) {
            throw new RuntimeException("配置日志失败：" + e.getMessage());
        }
    }

    /**
     * 取消配置日志
     */
    public static void unconfigure() {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.stop();
            logger.info("取消配置日志成功。");
        } catch (Exception e) {
            throw new RuntimeException("取消配置日志失败：" + e.getMessage());
        }
    }

    /**
     * 获取配置值，先获取系统配置，没有再读mock.properties配置文件。
     *
     * @param key          配置Key
     * @param defaultValue 配置默认值
     * @return 配置值
     */
    public static String getProperty(String key, String defaultValue) {
        String property = System.getProperty(key);
        if (StringUtils.isBlank(property)) {
            property = properties.getProperty(key);
        }
        return StringUtils.isBlank(property) ? defaultValue : property;
    }

    /**
     * 获取JSON字符串的值
     *
     * @param jsonString JSON字符串，形如：{"k1":"v1","k2":"v2",...}或[{},{},...]
     * @param jsonPath   JSON路径，语法参考：<a href="https://github.com/json-path/JsonPath">JsonPath</a>
     * @return JSON字符串的值
     */
    public static <T> T getJsonValue(String jsonString, String jsonPath) {
        return read(jsonString, jsonPath);
    }

    /**
     * 读取文件最后N行
     *
     * @param fileFullPath 文件全路径
     * @param count        最后N行
     * @return 文件最后N行
     */
    public static String readLastLines(String fileFullPath, int count) {
        File file = new File(fileFullPath);
        StringBuilder stringBuilder = new StringBuilder();
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
            List<String> lines = reader.readLines(count);
            for (int i = lines.size(); i > 0; i--) {
                stringBuilder.append(lines.get(i - 1)).append("\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败：" + e.getMessage());
        }
    }

}
