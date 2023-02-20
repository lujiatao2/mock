package com.lujiatao.mock.api;

import com.lujiatao.mock.common.entity.MockApp;

import java.util.List;

/**
 * Mock应用接口
 *
 * @author 卢家涛
 */
public interface MockAppService {

    boolean add(MockApp mockApp);

    boolean modify(MockApp mockApp);

    MockApp getById(int id);

    List<MockApp> search(String appEnv, String appName, String ip, String port, String isEnable);

    List<MockApp> getAll();

    boolean refreshConfig(String ip, int port);

    String viewLog(String ip, int port);

    boolean checkHeartbeat(String ip, int port);

}
