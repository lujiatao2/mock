package com.lujiatao.mock.example.app;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Mock示例应用VO
 *
 * @author 卢家涛
 */
@Data
@AllArgsConstructor
public class MockExampleAppVO implements Serializable {

    private int id;

    private String stringParameter;

    private int intParameter;

    private Integer integerParameter;

    private double doubleParameter;

    private Double aDoubleParameter;

    private boolean booleanParameter;

    private Boolean aBooleanParameter;

}
