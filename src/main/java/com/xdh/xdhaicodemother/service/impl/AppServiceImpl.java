package com.xdh.xdhaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.mapper.AppMapper;
import com.xdh.xdhaicodemother.model.dto.app.AppQueryRequest;
import com.xdh.xdhaicodemother.model.entity.App;
import com.xdh.xdhaicodemother.model.entity.User;
import com.xdh.xdhaicodemother.model.vo.AppVO;
import com.xdh.xdhaicodemother.model.vo.UserVO;
import com.xdh.xdhaicodemother.service.AppService;
import com.xdh.xdhaicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author xdh
 * @since 2026-07-29
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
    @Resource
    UserService userService;

    /**
     * 获取脱敏的应用信息
     *
     * @param app 应用
     * @return 脱敏后的应用信息
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        // 关联查询用户信息
        if (app.getUserId() != null) {
            User user = userService.getById(app.getUserId());
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    /**
     * 获取脱敏的应用信息列表
     *
     * @param appList 应用列表
     * @return 脱敏后的应用信息列表
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    /**
     * 构造查询请求
     *
     * @param appQueryRequest 请求参数类
     * @return QueryWrapper
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is null");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", appQueryRequest.getId())
                .like("appName", appQueryRequest.getAppName())
                .like("cover", appQueryRequest.getCover())
                .like("initPrompt", appQueryRequest.getInitPrompt())
                .eq("codeGenType", appQueryRequest.getCodeGenType())
                .eq("deployKey", appQueryRequest.getDeployKey())
                .eq("priority", appQueryRequest.getPriority())
                .eq("userId", appQueryRequest.getUserId())
                .orderBy(appQueryRequest.getSortField(), "ascend".equals(appQueryRequest.getSortOrder()));
        if (CharSequenceUtil.isNotBlank(appQueryRequest.getSortField())) {
            queryWrapper.orderBy(appQueryRequest.getSortField(), "ascend".equals(appQueryRequest.getSortOrder()));
        } else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }
}
