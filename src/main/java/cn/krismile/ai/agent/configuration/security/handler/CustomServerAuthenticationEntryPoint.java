package cn.krismile.ai.agent.configuration.security.handler;

import cn.krismile.ai.agent.util.ObjectMapperUtils;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 自定义认证入口点
 * <p>
 * 用于处理未认证异常（401 Unauthorized），返回统一的 JSON 格式响应。
 * </p>
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建统一响应体
        VO<?> result = R.fail(ErrorCodeEnum.ACCESS_UNAUTHORIZED);
        String body = ObjectMapperUtils.writeValueAsStringFailSafe(result);
        if (body == null) {
            body = "{\"code\":401,\"msg\":\"Unauthorized\",\"data\":null}";
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
