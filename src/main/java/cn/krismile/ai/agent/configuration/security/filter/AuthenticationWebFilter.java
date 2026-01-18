package cn.krismile.ai.agent.configuration.security.filter;

import cn.krismile.ai.agent.configuration.security.context.UserContextThreadLocalAccessor;
import cn.krismile.ai.agent.configuration.security.jwt.JwtTokenProvider;
import host.springboot.framework3.core.logging.LoggingComponent;
import host.springboot.framework3.core.model.Pair;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Collections;
import java.util.List;

/**
 * 认证拦截器
 * <p>
 * 从请求 Header 中提取 JWT Token，完成校验并在 Reactor 上下文中写入认证信息。
 * 同时将用户 ID 写入上下文，配合线程上下文桥接实现同步场景兼容。
 * </p>
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AuthenticationWebFilter implements WebFilter, LoggingComponent {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 初始化上下文桥接
     *
     * @since 1.0.0
     */
    @PostConstruct
    public void init() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new UserContextThreadLocalAccessor());
        Hooks.enableAutomaticContextPropagation();
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String token = this.resolveToken(exchange.getRequest());

        if (StringUtils.isBlank(token)) {
            logInstance().debug("请求未携带Token", List.of(Pair.of("path", path)));
            return chain.filter(exchange);
        }

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                logInstance().warn("认证失败：无效Token", List.of(
                        Pair.of("path", path),
                        Pair.of("tokenLength", token.length())
                ));
                return chain.filter(exchange);
            }

            Long userId = jwtTokenProvider.getUserId(token);
            logInstance().debug("认证成功", List.of(
                    Pair.of("path", path),
                    Pair.of("userId", userId)
            ));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId, token, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                    .contextWrite(Context.of(UserContextThreadLocalAccessor.KEY, userId));
        } catch (Exception e) {
            logInstance().error("认证失败：解析Token异常", e, List.of(
                    Pair.of("path", path),
                    Pair.of("tokenLength", token.length())
            ));
            return chain.filter(exchange);
        }
    }

    /**
     * 从 Header 中解析 Bearer Token
     *
     * @param request 请求对象
     * @return Token（不包含 Bearer 前缀），不存在则返回 null
     * @since 1.0.0
     */
    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public @NonNull String logTag() {
        return "认证拦截器";
    }
}
