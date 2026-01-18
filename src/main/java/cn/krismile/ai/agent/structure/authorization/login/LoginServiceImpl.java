package cn.krismile.ai.agent.structure.authorization.login;

import cn.krismile.ai.agent.configuration.security.jwt.JwtTokenProvider;
import cn.krismile.ai.agent.model.enumeration.UserStatusEnum;
import cn.krismile.ai.agent.model.request.login.LoginByUsernameRequest;
import cn.krismile.ai.agent.repository.user.UserRepository;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 登录服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final String USER_NOT_EXIST = "用户不存在";
    private static final String PASSWORD_ERROR = "密码错误";
    private static final String USER_DISABLED = "用户已被禁用";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<String> loginByUsername(LoginByUsernameRequest request) {
        String username = request.username();
        String password = request.password();

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ApplicationException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST, USER_NOT_EXIST)))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new ApplicationException(ErrorCodeEnum.USER_PASSWORD_VERIFY_FAILED, PASSWORD_ERROR));
                    }
                    if (user.getStatus() == UserStatusEnum.DISABLED) {
                        return Mono.error(new ApplicationException(ErrorCodeEnum.USER_ACCOUNT_FROZEN, USER_DISABLED));
                    }
                    String token = jwtTokenProvider.createToken(user.getId());
                    return Mono.just(token);
                });
    }
}
