package com.lujiatao.mock.web.controller;

import com.lujiatao.mock.api.MockConfigService;
import com.lujiatao.mock.common.entity.MockConfig;
import com.lujiatao.mock.common.model.BaseVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lujiatao.mock.web.util.CommonUtil.getTargetData;

/**
 * Mock配置控制器
 *
 * @author 卢家涛
 */
@RestController
@RequestMapping("/mock-config")
@ResponseBody
public class MockConfigController {

    private final MockConfigService mockConfigService;

    @Autowired
    public MockConfigController(MockConfigService mockConfigService) {
        this.mockConfigService = mockConfigService;
    }

    @GetMapping("/all")
    public BaseVO all() {
        List<MockConfig> mockConfigs = mockConfigService.getAll();
        return getTargetData(mockConfigs, 1);
    }

    @GetMapping("/page")
    public BaseVO page(@RequestParam("appEnv") String appEnv, @RequestParam("appName") String appName, @RequestParam("mockClass") String mockClass, @RequestParam("mockMethod") String mockMethod, @RequestParam("isEnable") String isEnable, @RequestParam("targetPage") int targetPage) {
        List<MockConfig> mockConfigs = mockConfigService.search(appEnv, appName, mockClass, mockMethod, isEnable);
        return getTargetData(mockConfigs, targetPage);
    }

    @GetMapping("/search")
    public BaseVO search(@RequestParam("appEnv") String appEnv, @RequestParam("appName") String appName, @RequestParam("mockClass") String mockClass, @RequestParam("mockMethod") String mockMethod, @RequestParam("isEnable") String isEnable) {
        List<MockConfig> mockConfigs = mockConfigService.search(appEnv, appName, mockClass, mockMethod, isEnable);
        return getTargetData(mockConfigs, 1);
    }

    @PostMapping
    public BaseVO add(@RequestBody MockConfig mockConfig) {
        int id = mockConfig.getId();
        MockConfig tmp = mockConfigService.getById(id);
        if (tmp == null) {
            return mockConfigService.add(mockConfig) ? new BaseVO() : new BaseVO("添加Mock配置失败。");
        } else {
            return new BaseVO("该记录已存在。");
        }
    }

    @PutMapping
    public BaseVO modify(@RequestBody MockConfig mockConfig) {
        int id = mockConfig.getId();
        MockConfig tmp = mockConfigService.getById(id);
        if (tmp == null) {
            return new BaseVO("该记录不存在。");
        } else {
            String appEnv = mockConfig.getAppEnv();
            boolean isEnable = mockConfig.isEnable();
            // 启用/禁用（前端只会传ID）
            if (StringUtils.isEmpty(appEnv)) {
                tmp.setEnable(isEnable);
                return mockConfigService.modify(tmp) ? new BaseVO() : new BaseVO(isEnable ? "启用Mock配置失败。" : "禁用Mock配置失败。");
            }
            // 修改
            else {
                return mockConfigService.modify(mockConfig) ? new BaseVO() : new BaseVO("修改Mock配置失败。");
            }
        }
    }

    @DeleteMapping
    public BaseVO delete(@RequestParam("id") int id) {
        MockConfig tmp = mockConfigService.getById(id);
        if (tmp == null) {
            return new BaseVO("该记录不存在。");
        } else {
            return mockConfigService.deleteById(id) ? new BaseVO() : new BaseVO("删除Mock配置失败。");
        }
    }

}
