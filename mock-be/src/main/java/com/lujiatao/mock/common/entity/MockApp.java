package com.lujiatao.mock.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Mock应用
 *
 * @author 卢家涛
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class MockApp extends BasePO {

    private int id;

    private String appEnv;

    private String appName;

    private String ip;

    private int port;

    private String version;

    private boolean isEnable;

}
