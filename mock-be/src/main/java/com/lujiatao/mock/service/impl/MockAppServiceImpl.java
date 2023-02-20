package com.lujiatao.mock.service.impl;

import com.lujiatao.mock.api.MockAppService;
import com.lujiatao.mock.common.entity.MockApp;
import com.lujiatao.mock.module.util.HttpUtil;
import com.lujiatao.mock.service.repository.MockAppDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mock应用接口实现
 *
 * @author 卢家涛
 */
@Service
public class MockAppServiceImpl implements MockAppService {

    private static final Logger logger = LoggerFactory.getLogger(MockAppServiceImpl.class);

    private final MockAppDao mockAppDao;

    @Autowired
    public MockAppServiceImpl(MockAppDao mockAppDao) {
        this.mockAppDao = mockAppDao;
    }

    @Override
    public boolean add(MockApp mockApp) {
        return mockAppDao.insert(mockApp);
    }

    @Override
    public boolean modify(MockApp mockApp) {
        return mockAppDao.update(mockApp);
    }

    @Override
    public MockApp getById(int id) {
        return mockAppDao.selectById(id);
    }

    @Override
    public List<MockApp> search(String appEnv, String appName, String ip, String port, String isEnable) {
        return mockAppDao.select(appEnv, appName, ip, port, isEnable);
    }

    @Override
    public List<MockApp> getAll() {
        return mockAppDao.selectAll();
    }

    @Override
    public boolean refreshConfig(String ip, int port) {
        try (HttpUtil httpClient = new HttpUtil.HttpUtilBuilder(String.format("http://%s:%s/sandbox/default/module/http/mock/refresh-config", ip, port)).build()) {
            String response = httpClient.execute().getResponseAsString();
            if (response.equals("true")) {
                logger.info("刷新配置成功。");
                return true;
            } else {
                throw new RuntimeException("刷新配置失败。");
            }
        } catch (Exception e) {
            logger.error("刷新配置失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public String viewLog(String ip, int port) {
        try (HttpUtil httpClient = new HttpUtil.HttpUtilBuilder(String.format("http://%s:%s/sandbox/default/module/http/mock/view-log", ip, port)).build()) {
            return httpClient.execute().getResponseAsString();
        } catch (Exception e) {
            logger.error("查看日志失败：{}", e.getMessage());
            return "";
        }
    }

    @Override
    public boolean checkHeartbeat(String ip, int port) {
        try (HttpUtil httpClient = new HttpUtil.HttpUtilBuilder(String.format("http://%s:%s/sandbox/default/module/http/mock/check-heartbeat", ip, port)).build()) {
            String response = httpClient.execute().getResponseAsString();
            if (response.equals("true")) {
                logger.info("校验心跳成功。");
                return true;
            } else {
                throw new RuntimeException("校验心跳失败。");
            }
        } catch (Exception e) {
            logger.error("校验心跳失败：{}", e.getMessage());
            return false;
        }
    }

}
