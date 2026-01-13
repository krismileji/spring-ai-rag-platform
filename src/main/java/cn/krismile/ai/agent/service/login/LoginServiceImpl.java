package cn.krismile.ai.agent.service.login;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.model.domain.UserDO;
import cn.krismile.ai.agent.model.enumeration.UserStatusEnum;
import cn.krismile.ai.agent.model.request.login.LoginByUsernameRequest;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static cn.krismile.ai.agent.model.domain.table.UserDOTableDef.USER_DO;

/**
 * 登录服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public String loginByUsername(LoginByUsernameRequest request) {
        String username = request.username();
        String password = request.password();

        UserDO user = UserDO.create()
                .where(USER_DO.USERNAME.eq(username))
                .oneOpt().orElseThrow(() -> new ApplicationException(
                        ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST, "用户不存在"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApplicationException(ErrorCodeEnum.USER_PASSWORD_VERIFY_FAILED, "密码错误");
        }
        if (user.getStatus() == UserStatusEnum.DISABLED) {
            throw new ApplicationException(ErrorCodeEnum.USER_ACCOUNT_FROZEN, "用户已被禁用");
        }
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }
}
