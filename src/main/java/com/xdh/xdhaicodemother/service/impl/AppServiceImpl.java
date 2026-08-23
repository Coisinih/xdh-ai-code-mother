package com.xdh.xdhaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xdh.xdhaicodemother.constant.AppConstant;
import com.xdh.xdhaicodemother.core.AiCodeGeneratorFacade;
import com.xdh.xdhaicodemother.core.builder.VueProjectBuilder;
import com.xdh.xdhaicodemother.core.handle.StreamHandlerExecutor;
import com.xdh.xdhaicodemother.exception.BusinessException;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.mapper.AppMapper;
import com.xdh.xdhaicodemother.model.dto.app.AppQueryRequest;
import com.xdh.xdhaicodemother.model.entity.App;
import com.xdh.xdhaicodemother.model.entity.User;
import com.xdh.xdhaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import com.xdh.xdhaicodemother.model.vo.AppVO;
import com.xdh.xdhaicodemother.model.vo.UserVO;
import com.xdh.xdhaicodemother.service.AppService;
import com.xdh.xdhaicodemother.service.ChatHistoryService;
import com.xdh.xdhaicodemother.service.ScreenshotService;
import com.xdh.xdhaicodemother.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author xdh
 * @since 2026-07-29
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
    @Resource
    UserService userService;

    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private VueProjectBuilder vueProjectBuilder;


    /**
     * 通过对话生成应用代码
     *
     * @param userMessage 用户提示词
     * @param appId       应用id
     * @param loginUser   登录用户
     * @return Flux<String> 生成代码的流
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

        // 4.1 保存用户消息
        chatHistoryService.addChatMessage(userMessage, loginUser.getId(), appId, ChatHistoryMessageTypeEnum.USER.getValue());

        // 5. 调用 AI 生成应用代码
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(codeGenType, userMessage, appId);
        // 5.1 收集 AI 响应内容，并在完成后记录到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenType);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID 不能为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() <= 0, ErrorCode.NOT_LOGIN_ERROR, "用户未登入");

        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3.权限校验，仅本人可以部署自己的应用
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");

        // 4.查询是否有deployKey，没有的话就生成一个 6 位的随机（数字 + 字符）字符串
        String deployKey = app.getDeployKey();
        if (CharSequenceUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        // 5.校验路径是否存在
        String baseAppPath = CharSequenceUtil.format("{}{}{}_{}", AppConstant.CODE_OUTPUT_ROOT_DIR, File.separator, app.getCodeGenType(), app.getId());
        ThrowUtils.throwIf(!FileUtil.exist(baseAppPath) || !FileUtil.isDirectory(baseAppPath), ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成应用代码");

        // 6. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        if (CodeGenTypeEnum.VUE_PROJECT == codeGenType) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(baseAppPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 验证 dist 目录是否生成
            File distDir = new File(baseAppPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            baseAppPath = baseAppPath + File.separator + "dist";
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }

        // 7.复制代码文件目录到部署目录
        try {
            String deployPath = CharSequenceUtil.format("{}{}{}", AppConstant.CODE_DEPLOY_ROOT_DIR, File.separator, deployKey);
            FileUtil.copyContent(new File(baseAppPath), new File(deployPath), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }

        // 8.更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

        // 9.返回部署后可访问的 URL 地址
        String deployedUrl = CharSequenceUtil.format("{}/{}/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 10.应用截图并更新封面
        generateAppScreenshotAsync(deployedUrl, appId);
        return deployedUrl;
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    private void generateAppScreenshotAsync(String appUrl, long appId) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String cosUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(cosUrl);
            boolean updateResult = this.updateById(updateApp);
            ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用封面失败");
        });
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
        Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, userService::getUserVO));
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

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        // try-catch 即使消息记录删除失败了，也不会影响应用的删除
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }

}
