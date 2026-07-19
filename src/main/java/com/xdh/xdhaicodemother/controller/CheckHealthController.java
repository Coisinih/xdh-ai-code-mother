package com.xdh.xdhaicodemother.controller;

import com.xdh.xdhaicodemother.common.BaseResponse;
import com.xdh.xdhaicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 * @author huanglina
 * date：  2026/7/19
 */
@RestController
@RequestMapping("/health")
public class CheckHealthController {
    @GetMapping("/")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
