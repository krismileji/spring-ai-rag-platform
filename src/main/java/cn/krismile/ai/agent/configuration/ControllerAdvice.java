package cn.krismile.ai.agent.configuration;

import cn.dev33.satoken.exception.*;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.logging.LoggingComponent;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import org.jspecify.annotations.NonNull;
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

    @ExceptionHandler(NotLoginException.class)
    public Mono<VO<?>> handlerException(NotLoginException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-未登录 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-未登录 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e);
        log().warn("[{}] -------------------------------- Authorization-未登录 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.ACCESS_UNAUTHORIZED, "请先登录"));
    }

    @ExceptionHandler(NotPermissionException.class)
    public Mono<VO<?>> handlerException(NotPermissionException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-缺少权限 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-缺少权限 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}, permission: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e.getPermission(), e);
        log().warn("[{}] -------------------------------- Authorization缺少权限 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.USER_IDENTITY_VERIFICATION_FAILED, "缺少权限无法访问"));
    }

    @ExceptionHandler(NotRoleException.class)
    public Mono<VO<?>> handlerException(NotRoleException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-缺少角色 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-缺少角色 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}, role: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e.getRole(), e);
        log().warn("[{}] -------------------------------- Authorization-缺少角色 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.USER_IDENTITY_VERIFICATION_FAILED, "缺少角色无法访问"));
    }

    @ExceptionHandler(NotSafeException.class)
    public Mono<VO<?>> handlerException(NotSafeException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-授权失败 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-授权失败 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}, service: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e.getService(), e);
        log().warn("[{}] -------------------------------- Authorization-授权失败 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.USER_AUTHORITY_REJECTED, "授权失败"));
    }

    @ExceptionHandler(DisableServiceException.class)
    public Mono<VO<?>> handlerException(DisableServiceException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-已被封禁 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-已被封禁 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}, service: {}, level: {}, disabledTime: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e.getService(), e.getLevel(), e.getDisableTime(), e);
        log().warn("[{}] -------------------------------- Authorization-已被封禁 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.ACCOUNT_IS_FROZEN, String.format("账号已被封禁， %d 分钟后解封", e.getDisableTime() / 60)));
    }

    @ExceptionHandler(NotHttpBasicAuthException.class)
    public Mono<VO<?>> handlerException(NotHttpBasicAuthException e, ServerWebExchange exchange) {
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        log().warn("[{}] -------------------------------- Authorization-Basic认证失败 -------------------------------- [Begin]", logTag());
        log().error("[{}] Authorization-Basic认证失败 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                logTag(), exchange.getRequest().getURI(), exchange.getRequest().getMethod(), clientIp, getRequestInfo(exchange), e.getLocalizedMessage(), e);
        log().warn("[{}] -------------------------------- Authorization-Basic认证失败 -------------------------------- [ End ]", logTag());
        return Mono.just(R.fail(ErrorCodeEnum.USER_ACCESS_BLOCKED, "认证失败"));
    }

    /**
     * Build a compact request info string from ServerWebExchange for logging in reactive handlers.
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
