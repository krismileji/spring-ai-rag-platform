package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.model.request.chat.ChatPlatformEditRequest;
import cn.krismile.ai.agent.model.response.chat.ChatPlatformVO;
import cn.krismile.ai.agent.structure.chat.platoform.PlatformService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 聊天平台控制器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/platform")
@Tag(name = "聊天平台", description = "聊天平台")
public class ChatPlatformController {

    @Resource
    private PlatformService platformService;

    @Operation(summary = "聊天平台列表")
    @GetMapping("/chat/list")
    public VO<List<ChatPlatformVO>> listPlatforms() {
        return R.data(this.platformService.listPlatforms());
    }

    @Operation(summary = "编辑聊天平台")
    @PutMapping("/chat/edit")
    public VO<Boolean> editPlatform(@RequestBody @Valid ChatPlatformEditRequest request) {
        return R.data(this.platformService.editPlatform(request));
    }
}