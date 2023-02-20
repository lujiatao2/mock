package com.lujiatao.mock.service.repository;

import com.lujiatao.mock.common.entity.MockApp;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Mock应用DAO
 *
 * @author 卢家涛
 */
@Mapper
public interface MockAppDao {

    boolean insert(MockApp mockApp);

    boolean update(MockApp mockApp);

    MockApp selectById(int id);

    List<MockApp> select(String appEnv, String appName, String ip, String port, String isEnable);

    List<MockApp> selectAll();

}
