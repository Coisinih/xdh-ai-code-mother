package com.xdh.xdhaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新应用请求
 */
@Data
public class AppUserUpdateRequest implements Serializable {

    /**
     * Id.
     */
    private Long id;

    /**
     * App name.
     */
    private String appName;

    @Serial
    private static final long serialVersionUID = 1L;
}
