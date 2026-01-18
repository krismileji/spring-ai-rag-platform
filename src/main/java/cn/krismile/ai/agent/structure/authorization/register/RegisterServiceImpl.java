package cn.krismile.ai.agent.structure.authorization.register;

import cn.krismile.ai.agent.configuration.security.jwt.JwtTokenProvider;
import cn.krismile.ai.agent.model.domain.UserDO;
import cn.krismile.ai.agent.model.enumeration.UserStatusEnum;
import cn.krismile.ai.agent.model.request.register.RegisterByUsernameRequest;
import cn.krismile.ai.agent.repository.user.UserRepository;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * 注册服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Mono<String> registerByUsername(RegisterByUsernameRequest request) {
        String username = request.username();
        String password = request.password();

        return this.isRegistered(username)
                .flatMap(registered -> {
                    if (registered) {
                        return Mono.error(new ApplicationException(ErrorCodeEnum.USERNAME_ALREADY_EXISTS));
                    }
                    String encodedPassword = passwordEncoder.encode(password);
                    UserDO user = new UserDO();
                    user.setUsername(username);
                    user.setPassword(encodedPassword);
                    user.setStatus(UserStatusEnum.NORMAL);
                    return userRepository.save(user);
                })
                .map(user -> jwtTokenProvider.createToken(user.getId()));
    }

    @Override
    public Mono<Boolean> isRegistered(String username) {
        return userRepository.findByUsername(username).hasElement();
    }
}
