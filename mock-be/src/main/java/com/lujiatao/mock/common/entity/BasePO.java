package com.lujiatao.mock.common.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础PO
 *
 * @author 卢家涛
 */
@Data
public class BasePO implements Serializable {

    private String createTime;

    private String updateTime;

}
