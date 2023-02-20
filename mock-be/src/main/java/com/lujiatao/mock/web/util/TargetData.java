package com.lujiatao.mock.web.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 目标数据
 *
 * @author 卢家涛
 */
@Data
@AllArgsConstructor
public class TargetData implements Serializable {

    private List<?> targetData;

    private int currentPage;

    private int totalPage;

}
