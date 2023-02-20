package com.lujiatao.mock.api;

import com.lujiatao.mock.common.entity.MockConfig;

import java.util.List;

/**
 * Mock配置接口
 *
 * @author 卢家涛
 */
public interface MockConfigService {

    boolean add(MockConfig mockConfig);

    boolean modify(MockConfig mockConfig);

    boolean deleteById(int id);

    MockConfig getById(int id);

    List<MockConfig> search(String appEnv, String appName, String mockClass, String mockMethod, String isEnable);

    List<MockConfig> getAll();

}
