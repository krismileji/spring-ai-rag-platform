package cn.krismile.ai.agent.controller.user;

import cn.krismile.ai.agent.model.request.login.LoginByUsernameRequest;
import cn.krismile.ai.agent.structure.authorization.login.LoginService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 注册控制器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/login")
@Tag(name = "用户登录", description = "用户登录")
public class LoginController {

    @Resource
    private LoginService loginService;

    /**
     * 根据用户名登录
     *
     * @param request 登录请求参数
     * @return 登录token
     * @since 1.0.0
     */
    @Operation(summary = "根据用户名登录")
    @PostMapping("/username")
    public Mono<VO<String>> loginByUsername(@RequestBody LoginByUsernameRequest request) {
        return loginService.loginByUsername(request).map(R::data).defaultIfEmpty(R.data((String) null));
    }
}
