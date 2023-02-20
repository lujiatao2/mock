package com.lujiatao.mock.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Mock配置
 *
 * @author 卢家涛
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class MockConfig extends BasePO {

    private int id;

    private String appEnv;

    private String appName;

    private String mockClass;

    private String mockMethod;

    private String parameterRules;

    private String returnOrThrowData;

    private boolean isEnable;

}
