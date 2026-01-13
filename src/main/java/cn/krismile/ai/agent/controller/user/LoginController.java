package cn.krismile.ai.agent.controller.user;

import cn.krismile.ai.agent.model.request.login.LoginByUsernameRequest;
import cn.krismile.ai.agent.service.login.LoginService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "根据用户名登录")
    @PostMapping("/username")
    public VO<String> loginByUsername(@RequestBody LoginByUsernameRequest request) {
        return R.data(loginService.loginByUsername(request));
    }
}
