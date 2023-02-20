package com.lujiatao.mock.module.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mock配置
 *
 * @author 卢家涛
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MockConfig {

    private String mockClass;

    private String mockMethod;

    private ParameterRule[] parameterRules;

    private String returnOrThrowData;

}
