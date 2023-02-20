package com.lujiatao.mock.service.impl;

import com.lujiatao.mock.api.MockConfigService;
import com.lujiatao.mock.common.entity.MockConfig;
import com.lujiatao.mock.service.repository.MockConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mock配置接口实现
 *
 * @author 卢家涛
 */
@Service
public class MockConfigImpl implements MockConfigService {

    private final MockConfigDao mockConfigDao;

    @Autowired
    public MockConfigImpl(MockConfigDao mockConfigDao) {
        this.mockConfigDao = mockConfigDao;
    }

    @Override
    public boolean add(MockConfig mockConfig) {
        return mockConfigDao.insert(mockConfig);
    }

    @Override
    public boolean modify(MockConfig mockConfig) {
        return mockConfigDao.update(mockConfig);
    }

    @Override
    public boolean deleteById(int id) {
        return mockConfigDao.deleteById(id);
    }

    @Override
    public MockConfig getById(int id) {
        return mockConfigDao.selectById(id);
    }

    @Override
    public List<MockConfig> search(String appEnv, String appName, String mockClass, String mockMethod, String isEnable) {
        return mockConfigDao.select(appEnv, appName, mockClass, mockMethod, isEnable);
    }

    @Override
    public List<MockConfig> getAll() {
        return mockConfigDao.selectAll();
    }

}
