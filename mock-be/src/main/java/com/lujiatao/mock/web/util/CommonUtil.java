package com.lujiatao.mock.web.util;

import com.lujiatao.mock.common.model.BaseVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 公共工具
 *
 * @author 卢家涛
 */
public class CommonUtil {

    /**
     * 获取目标页数据（每页10条）
     *
     * @param totalData  总数据
     * @param targetPage 目标页码
     * @return 目标数据、当前页码和总页码
     */
    public static BaseVO getTargetData(List<?> totalData, int targetPage) {
        return getTargetData(totalData, targetPage, 10);
    }

    /**
     * 获取目标页数据
     *
     * @param totalData  总数据
     * @param targetPage 目标页码
     * @param pageSize   每页数量
     * @return 目标数据、当前页码和总页码
     */
    public static BaseVO getTargetData(List<?> totalData, int targetPage, int pageSize) {
        BaseVO baseVO = new BaseVO();
        if (totalData == null || totalData.isEmpty()) {
            baseVO.setData(new TargetData(new ArrayList<>(), 0, 0));
            return baseVO;
        }
        int currentPage;
        int totalPage = totalData.size() % pageSize == 0 ? totalData.size() / pageSize : totalData.size() / pageSize + 1;
        List<?> targetData;
        if (targetPage < 1) {
            currentPage = 1;
            targetData = totalData.subList(0, currentPage == totalPage ? totalData.size() : pageSize);
        } else if (targetPage > totalPage) {
            currentPage = totalPage;
            targetData = totalData.subList((currentPage - 1) * pageSize, totalData.size());
        } else {
            currentPage = targetPage;
            targetData = totalData.subList((currentPage - 1) * pageSize, currentPage == totalPage ? totalData.size() : currentPage * pageSize);
        }
        baseVO.setData(new TargetData(targetData, currentPage, totalPage));
        return baseVO;
    }

}
