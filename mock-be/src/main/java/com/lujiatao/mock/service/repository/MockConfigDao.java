package com.lujiatao.mock.service.repository;

import com.lujiatao.mock.common.entity.MockConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Mock配置DAO
 *
 * @author 卢家涛
 */
@Mapper
public interface MockConfigDao {

    boolean insert(MockConfig mockConfig);

    boolean update(MockConfig mockConfig);

    boolean deleteById(int id);

    MockConfig selectById(int id);

    List<MockConfig> select(String appEnv, String appName, String mockClass, String mockMethod, String isEnable);

    List<MockConfig> selectAll();

}
