package com.xdh.xdhaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户创建应用请求类
 */
@Data
public class AppAddRequest implements Serializable {


    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    @Serial
    private static final long serialVersionUID = 1L;
}
