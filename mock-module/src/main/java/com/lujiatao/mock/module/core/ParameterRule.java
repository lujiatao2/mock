package com.lujiatao.mock.module.core;

import com.lujiatao.mock.module.constant.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参数规则
 *
 * @author 卢家涛
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterRule {

    private String type;

    private Pattern pattern;

    private String value;

}
