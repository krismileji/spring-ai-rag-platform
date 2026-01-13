package cn.krismile.ai.agent.model.request.register;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 根据用户名注册用户请求
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "根据用户名注册用户请求")
public record RegisterByUsernameRequest(
        @Schema(description = "用户名")
        String username,
        @Schema(description = "密码")
        String password
) {
}
