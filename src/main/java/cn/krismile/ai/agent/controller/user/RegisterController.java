package cn.krismile.ai.agent.controller.user;

import cn.krismile.ai.agent.model.request.register.RegisterByUsernameRequest;
import cn.krismile.ai.agent.structure.authorization.register.RegisterService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

    /**
     * 校验用户名是否已注册
     *
     * @param username 用户名
     * @return 是否已注册
     * @since 1.0.0
     */
    @Operation(summary = "校验用户名")
    @GetMapping("/checkUsername")
    public Mono<VO<Boolean>> checkUsername(@RequestParam String username) {
        return registerService.isRegistered(username).map(R::data);
    }

    /**
     * 根据用户名注册
     *
     * @param request 注册请求参数
     * @return 注册结果信息
     * @since 1.0.0
     */
    @Operation(summary = "根据用户名注册")
    @PostMapping("/username")
    public Mono<VO<String>> registerByUsername(@RequestBody RegisterByUsernameRequest request) {
        return registerService.registerByUsername(request).map(R::data).defaultIfEmpty(R.data((String) null));
    }
}
