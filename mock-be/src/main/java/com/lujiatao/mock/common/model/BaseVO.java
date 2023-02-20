package com.lujiatao.mock.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础VO
 *
 * @author 卢家涛
 */
@Data
@AllArgsConstructor
public class BaseVO implements Serializable {

    private boolean success;
    private String message;
    private Object data;

    /**
     * 返回成功（带数据）的场景
     */
    public BaseVO(Object data) {
        this(true, "", data);
    }

    /**
     * 返回成功（不带数据）的场景
     */
    public BaseVO() {
        this(true, "", null);
    }

    /**
     * 返回失败的场景
     */
    public BaseVO(String message) {
        this(false, message, null);
    }

}
