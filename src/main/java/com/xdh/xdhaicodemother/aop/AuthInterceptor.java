package com.xdh.xdhaicodemother.aop;

import com.xdh.xdhaicodemother.annotation.AuthCheck;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.model.entity.User;
import com.xdh.xdhaicodemother.model.enums.UserRoleEnum;
import com.xdh.xdhaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author huanglina
 * date：  2026/7/26
 *
 * 权限校验 AOP 拦截器
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1.获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);

        // 2.无需权限直接放行
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(authCheck.mustRole());
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // 3.需要权限，校验是否有对应权限
        // 目前只有用户和管理员，只要登入了就有用户权限
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 需要管理员权限，校验是否有管理员权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum), ErrorCode.NO_AUTH_ERROR);

        return joinPoint.proceed();
    }
}
