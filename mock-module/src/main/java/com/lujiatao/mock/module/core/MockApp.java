package com.lujiatao.mock.module.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.lujiatao.mock.module.util.CommonUtil.getProperty;

/**
 * Mock应用
 *
 * @author 卢家涛
 */
@Data
@AllArgsConstructor
public class MockApp {

    private String appEnv;

    private String appName;

    private String ip;

    private String mockBeUrl;

    private static MockApp instance = new MockApp();

    private MockApp() {
        this.appEnv = getProperty("app.env", "unknown");
        this.appName = getProperty("app.name", "unknown");
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.ip = "127.0.0.1";
        }
        this.mockBeUrl = getProperty("mock.be.url", "http://127.0.0.1:80");
    }

    public static MockApp instance() {
        return instance;
    }

}
