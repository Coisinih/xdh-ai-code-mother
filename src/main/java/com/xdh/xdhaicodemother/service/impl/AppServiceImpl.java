package com.xdh.xdhaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xdh.xdhaicodemother.core.AiCodeGeneratorFacade;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.mapper.AppMapper;
import com.xdh.xdhaicodemother.model.dto.app.AppQueryRequest;
import com.xdh.xdhaicodemother.model.entity.App;
import com.xdh.xdhaicodemother.model.entity.User;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import com.xdh.xdhaicodemother.model.vo.AppVO;
import com.xdh.xdhaicodemother.model.vo.UserVO;
import com.xdh.xdhaicodemother.service.AppService;
import com.xdh.xdhaicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
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

    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;


    /**
     * 通过对话生成应用代码
     * @param userMessage   用户提示词
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return  Flux<String> 生成代码的流
     */
    @Override
    public Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(userMessage == null, ErrorCode.PARAMS_ERROR, "提示词不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");

        // 2.获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3.权限校验，仅本人可以生成代码
        ThrowUtils.throwIf(app.getUserId() == null || !Objects.equals(app.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");

        // 4.获取应用的生成类型
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "不支持的代码 生成类型");

        // 5.生成应用代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(codeGenType, userMessage, appId);
    }

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
