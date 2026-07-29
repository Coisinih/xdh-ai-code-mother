package com.xdh.xdhaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xdh.xdhaicodemother.model.dto.app.AppQueryRequest;
import com.xdh.xdhaicodemother.model.entity.App;
import com.xdh.xdhaicodemother.model.entity.User;
import com.xdh.xdhaicodemother.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author xdh
 * @since 2026-07-29
 */
public interface AppService extends IService<App> {
    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser);
}
