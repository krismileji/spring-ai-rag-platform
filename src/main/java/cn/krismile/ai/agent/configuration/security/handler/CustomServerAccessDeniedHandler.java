package cn.krismile.ai.agent.configuration.security.handler;

import cn.krismile.ai.agent.util.ObjectMapperUtils;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 自定义访问拒绝处理器
 * <p>
 * 用于处理权限不足异常（403 Forbidden），返回统一的 JSON 格式响应。
 * </p>
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class CustomServerAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建统一响应体
        VO<?> result = R.fail(ErrorCodeEnum.USER_IDENTITY_VERIFICATION_FAILED);
        String body = ObjectMapperUtils.writeValueAsStringFailSafe(result);
        if (body == null) {
            body = "{\"code\":403,\"msg\":\"Forbidden\",\"data\":null}";
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
