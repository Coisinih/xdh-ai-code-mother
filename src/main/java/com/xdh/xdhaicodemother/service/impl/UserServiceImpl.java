package com.xdh.xdhaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.mapper.UserMapper;
import com.xdh.xdhaicodemother.model.dto.user.UserQueryRequest;
import com.xdh.xdhaicodemother.model.entity.User;
import com.xdh.xdhaicodemother.model.enums.UserRoleEnum;
import com.xdh.xdhaicodemother.model.vo.LoginUserVO;
import com.xdh.xdhaicodemother.model.vo.UserVO;
import com.xdh.xdhaicodemother.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xdh.xdhaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author xdh
 * @since 2026-07-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userRegister(String userAccount, String password, String checkPassword) {
        // 1.输入参数校验
        ThrowUtils.throwIf(CharSequenceUtil.hasBlank(userAccount, password, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(password.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!password.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");

        // 2.校验是否已注册
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "用户账号已存在");

        // 3.用户密码加密处理
        String encryptPassword = getEncryptPassword(password);

        // 4.数据入库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "用户注册失败,数据库异常");

        // 5.返回用户id
        // mybatis-flex 插入数据后，会自动将数据回填到原来的实体类中
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String password) {
        // 盐值，用于混淆密码
        final String SALT = "xdh";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    /**
     * 对用户信息进行脱敏
     *
     * @param user 脱敏前完整的用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String password, HttpServletRequest request) {
        // 1.参数校验
        ThrowUtils.throwIf(CharSequenceUtil.hasBlank(userAccount, password), ErrorCode.PARAMS_ERROR, "用户名或密码不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账号错误");
        ThrowUtils.throwIf(password.length() < 8, ErrorCode.PARAMS_ERROR, "密码错误");
        // 2.密码加密
        String encryptPassword = getEncryptPassword(password);
        // 3.查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户名或密码错误");

        // 4.记录登入状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5.返回脱敏后的用户信息
        return getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1.会话中获取用户信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2.为了避免会话中缓存的不是最新的信息，所以获取id后从数据库中查询返回
        user = this.getById(user.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);

        // 3.返回脱敏后的用户信息
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 1.获取用户的登入状态
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObject == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2.如果用户已经登入，则移除登入状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) return new ArrayList<>();
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        return QueryWrapper.create()
                .eq("id", userQueryRequest.getId())
                .eq("userRole", userQueryRequest.getUserRole())
                .like("userName", userQueryRequest.getUserName())
                .like("userAccount", userQueryRequest.getUserAccount())
                .like("userProfile", userQueryRequest.getUserProfile())
                .orderBy(userQueryRequest.getSortField(), "ascend".equals(userQueryRequest.getSortOrder()));
    }
}
