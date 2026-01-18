package cn.krismile.ai.agent.configuration.security.context;

import host.springboot.framework3.core.exception.ApplicationException;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

/**
 * Security Utils
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class SecurityUtils {

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     * @since 1.0.0
     */
    public static Mono<Long> getUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> Long.parseLong(ctx.getAuthentication().getPrincipal().toString()))
                .switchIfEmpty(Mono.defer(() -> {
                    Long userId = UserContext.getUserId();
                    if (userId != null) {
                        return Mono.just(userId);
                    }
                    return Mono.error(new ApplicationException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST, "User not logged in"));
                }));
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     * @since 1.0.0
     */
    public static Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ApplicationException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST, "User not logged in (Sync Context)");
        }
        return userId;
    }
}
