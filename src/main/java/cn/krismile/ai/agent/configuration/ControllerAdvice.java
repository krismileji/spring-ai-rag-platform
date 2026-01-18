package cn.krismile.ai.agent.configuration;

import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.logging.LoggingComponent;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * ControllerAdvice
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
// @Component
@RestControllerAdvice
public class ControllerAdvice implements LoggingComponent {

    /**
     * 处理认证异常
     *
     * @param e 认证异常
     * @param exchange ServerWebExchange
     * @return 错误响应
     * @since 1.0.0
     */
    @ExceptionHandler(AuthenticationException.class)
    public Mono<VO<?>> handlerException(AuthenticationException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-未登录/认证失败 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-未登录/认证失败 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e);
        log().warn("[{}] -------------------------------- Authorization-未登录/认证失败 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.ACCESS_UNAUTHORIZED, "请先登录或认证失败"));
    }

    /**
     * 处理权限拒绝异常
     *
     * @param e 权限拒绝异常
     * @param exchange ServerWebExchange
     * @return 错误响应
     * @since 1.0.0
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<VO<?>> handlerException(AccessDeniedException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-无权限 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-无权限 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e);
        log().warn("[{}] -------------------------------- Authorization-无权限 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.USER_IDENTITY_VERIFICATION_FAILED, "无权限访问"));
    }

    /**
     * Build a compact request info string from ServerWebExchange for logging in reactive handlers.
     *
     * @param exchange ServerWebExchange
     * @return request info string
     * @since 1.0.0
     */
    private String getRequestInfo(ServerWebExchange exchange) {
        return String.format("uri=%s, method=%s, headers=%s",
                exchange.getRequest().getURI(),
                exchange.getRequest().getMethod(),
                exchange.getRequest().getHeaders().toSingleValueMap());
    }

    @Override
    public @NonNull String logTag() {
        return "请求异常拦截";
    }
}
