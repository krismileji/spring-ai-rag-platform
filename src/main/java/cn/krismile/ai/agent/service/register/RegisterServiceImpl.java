package cn.krismile.ai.agent.service.register;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.model.domain.UserDO;
import cn.krismile.ai.agent.model.enumeration.UserStatusEnum;
import cn.krismile.ai.agent.model.request.register.RegisterByUsernameRequest;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static cn.krismile.ai.agent.model.domain.table.UserDOTableDef.USER_DO;

/**
 * 注册服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class RegisterServiceImpl implements RegisterService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public String registerByUsername(RegisterByUsernameRequest request) {
        String username = request.username();
        String password = request.password();

        boolean registered = this.isRegistered(username);
        if (registered) {
            throw new ApplicationException(ErrorCodeEnum.USERNAME_ALREADY_EXISTS);
        }
        String encodedPassword = passwordEncoder.encode(password);
        UserDO user = UserDO.create()
                .setUsername(username)
                .setPassword(encodedPassword)
                .setStatus(UserStatusEnum.NORMAL)
                .saveOpt().orElseThrow(() -> new ApplicationException(
                        ErrorCodeEnum.USER_REGISTER_ERROR, "注册失败"));
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }

    @Override
    public boolean isRegistered(String username) {
        return UserDO.create().where(USER_DO.USERNAME.eq(username)).objOpt().isPresent();
    }
}
