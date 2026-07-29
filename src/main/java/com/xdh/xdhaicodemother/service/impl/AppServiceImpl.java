package com.xdh.xdhaicodemother.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xdh.xdhaicodemother.model.entity.App;
import com.xdh.xdhaicodemother.mapper.AppMapper;
import com.xdh.xdhaicodemother.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author xdh
 * @since 2026-07-29
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
