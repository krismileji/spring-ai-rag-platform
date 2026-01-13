package cn.krismile.ai.agent.controller.user;

import cn.krismile.ai.agent.model.request.register.RegisterByUsernameRequest;
import cn.krismile.ai.agent.service.register.RegisterService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 注册控制器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/register")
@Tag(name = "用户注册", description = "用户注册")
public class RegisterController {

    @Resource
    private RegisterService registerService;

    @Operation(summary = "校验用户名")
    @GetMapping("/checkUsername")
    public VO<Boolean> checkUsername(@RequestParam String username) {
        return R.data(registerService.isRegistered(username));
    }

    @Operation(summary = "根据用户名注册")
    @PostMapping("/username")
    public VO<String> registerByUsername(@RequestBody RegisterByUsernameRequest request) {
        return R.data(registerService.registerByUsername(request));
    }
}
