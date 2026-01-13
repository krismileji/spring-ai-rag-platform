package cn.krismile.ai.agent.context;

import org.springframework.web.server.ServerWebExchange;

import com.alibaba.ttl.TransmittableThreadLocal;

import cn.dev33.satoken.fun.SaRetGenericFunction;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;

/**
 * RequestContext
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class RequestContext {

    /**
     * 请求上下文
     */
    private static final TransmittableThreadLocal<ServerWebExchange> CONTEXT = new TransmittableThreadLocal<>();

    public static <T> T syncApply(SaRetGenericFunction<T> function) {
        ServerWebExchange exchange = CONTEXT.get();
        if (exchange == null) {
            return function.run();
        }
        return SaReactorSyncHolder.setContext(exchange, function);
    }

    public static void asyncApply(ServerWebExchange exchange, Runnable function) {
        try {
            CONTEXT.set(exchange);
            function.run();
        } finally {
            CONTEXT.remove();
        }
    }

    public static ServerWebExchange getExchange() {
        return CONTEXT.get();
    }

    public static void setContext(ServerWebExchange exchange) {
        CONTEXT.set(exchange);
    }

    public static void remove() {
        CONTEXT.remove();
    }
}
