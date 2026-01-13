package cn.krismile.ai.agent.configuration.authorization;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.krismile.ai.agent.context.RequestContext;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * ContextFilter
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
@Order
public class RequestContextFilter implements WebFilter {

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        RequestContext.setContext(exchange);
        SaReactorSyncHolder.setContext(exchange);
        return chain.filter(exchange).doFinally(type -> {
            RequestContext.remove();
            SaReactorSyncHolder.clearContext();
        });
    }
}
