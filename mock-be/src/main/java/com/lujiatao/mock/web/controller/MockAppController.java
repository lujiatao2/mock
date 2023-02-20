package com.lujiatao.mock.web.controller;

import com.lujiatao.mock.api.MockAppService;
import com.lujiatao.mock.common.entity.MockApp;
import com.lujiatao.mock.common.model.BaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.lujiatao.mock.web.util.CommonUtil.getTargetData;

/**
 * Mock应用控制器
 *
 * @author 卢家涛
 */
@RestController
@RequestMapping("/mock-app")
@ResponseBody
public class MockAppController {

    private final MockAppService mockAppService;

    @Autowired
    public MockAppController(MockAppService mockAppService) {
        this.mockAppService = mockAppService;
    }

    @GetMapping("/app-env")
    public BaseVO appEnv() {
        // 获取所有Mock应用环境并去重
        List<MockApp> mockApps = mockAppService.getAll();
        Set<String> appEnvs = new HashSet<>();
        for (MockApp mockApp : mockApps) {
            appEnvs.add(mockApp.getAppEnv());
        }
        return new BaseVO(new ArrayList<>(appEnvs));
    }

    @GetMapping("/app-name/{appEnv}")
    public BaseVO appName(@PathVariable String appEnv) {
        // 获取指定Mock应用环境下所有Mock应用名称并去重
        List<MockApp> mockApps = mockAppService.search(appEnv, "", "", "", "");
        Set<String> appNames = new HashSet<>();
        for (MockApp mockApp : mockApps) {
            appNames.add(mockApp.getAppName());
        }
        return new BaseVO(new ArrayList<>(appNames));
    }

    @GetMapping("/{appEnv}/{appName}/{ip}/{port}")
    public BaseVO getMockAppId(@PathVariable String appEnv, @PathVariable String appName, @PathVariable String ip, @PathVariable String port) {
        List<MockApp> mockApps = mockAppService.search(appEnv, appName, ip, port, "");
        return new BaseVO(mockApps.isEmpty() ? 0 : mockApps.get(0).getId());
    }

    @GetMapping("/all")
    public BaseVO all() {
        List<MockApp> mockApps = mockAppService.getAll();
        return getTargetData(mockApps, 1);
    }

    @GetMapping("/page")
    public BaseVO page(@RequestParam("appEnv") String appEnv, @RequestParam("appName") String appName, @RequestParam("isEnable") String isEnable, @RequestParam("targetPage") int targetPage) {
        List<MockApp> mockApps = mockAppService.search(appEnv, appName, "", "", isEnable);
        return getTargetData(mockApps, targetPage);
    }

    @GetMapping("/search")
    public BaseVO search(@RequestParam("appEnv") String appEnv, @RequestParam("appName") String appName, @RequestParam("isEnable") String isEnable) {
        List<MockApp> mockApps = mockAppService.search(appEnv, appName, "", "", isEnable);
        return getTargetData(mockApps, 1);
    }

    @PostMapping
    public BaseVO add(@RequestBody MockApp mockApp) {
        int id = mockApp.getId();
        MockApp tmp = mockAppService.getById(id);
        if (tmp == null) {
            return mockAppService.add(mockApp) ? new BaseVO() : new BaseVO("添加Mock应用失败。");
        } else {
            return new BaseVO("该记录已存在。");
        }
    }

    @PutMapping
    public BaseVO modify(@RequestBody MockApp mockApp) {
        int id = mockApp.getId();
        MockApp tmp = mockAppService.getById(id);
        if (tmp == null) {
            return new BaseVO("该记录不存在。");
        } else {
            return mockAppService.modify(mockApp) ? new BaseVO() : new BaseVO("修改Mock应用失败。");
        }
    }

    @GetMapping("/refresh-config/{id}")
    public BaseVO refreshConfig(@PathVariable int id) {
        MockApp mockApp = mockAppService.getById(id);
        if (mockApp == null) {
            return new BaseVO("该记录不存在。");
        } else {
            return mockAppService.refreshConfig(mockApp.getIp(), mockApp.getPort()) ? new BaseVO() : new BaseVO("刷新配置失败。");
        }
    }

    @GetMapping("/view-log/{id}")
    public BaseVO viewLog(@PathVariable int id) {
        MockApp mockApp = mockAppService.getById(id);
        if (mockApp == null) {
            return new BaseVO("该记录不存在。");
        } else {
            return new BaseVO((Object) mockAppService.viewLog(mockApp.getIp(), mockApp.getPort()));
        }
    }

}
