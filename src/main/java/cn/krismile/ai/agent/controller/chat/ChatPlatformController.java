package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import cn.krismile.ai.agent.model.request.chat.ChatModelEditRequest;
import cn.krismile.ai.agent.model.request.chat.ChatPlatformEditRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.chat.ChatPlatformVO;
import cn.krismile.ai.agent.structure.chat.platoform.service.PlatformService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
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

    /**
     * 查询聊天平台列表
     *
     * @return 聊天平台列表
     * @since 1.0.0
     */
    @Operation(summary = "聊天平台列表")
    @GetMapping("/chat/list")
    public Mono<VO<List<ChatPlatformVO>>> listPlatforms() {
        return this.platformService.listPlatforms().map(R::data);
    }

    /**
     * 编辑聊天平台
     *
     * @param request 编辑请求参数
     * @return 是否编辑成功
     * @since 1.0.0
     */
    @Operation(summary = "编辑聊天平台")
    @PutMapping("/chat/edit")
    public Mono<VO<Boolean>> editPlatform(@RequestBody @Valid ChatPlatformEditRequest request) {
        return this.platformService.editPlatform(request).map(R::data).defaultIfEmpty(R.data(false));
    }

    /**
     * 查询聊天模型列表
     *
     * @param type 聊天模型类型
     * @return 聊天模型列表
     * @since 1.0.0
     */
    @Operation(summary = "聊天模型列表")
    @GetMapping("/model/{type}/list")
    public Mono<VO<List<ChatModelVO>>> listChatModels(@PathVariable ChatModelTypeEnum type) {
        return this.platformService.listModels(type).collectList().map(R::data);
    }

    /**
     * 编辑聊天模型
     *
     * @param type    聊天模型类型
     * @param request 编辑请求参数
     * @return 是否编辑成功
     * @since 1.0.0
     */
    @Operation(summary = "编辑聊天模型")
    @PutMapping("/model/{type}/edit")
    public Mono<VO<Boolean>> editModel(
            @PathVariable ChatModelTypeEnum type,
            @RequestBody @Valid ChatModelEditRequest request) {
        return this.platformService.editModel(type, request).map(R::data).defaultIfEmpty(R.data(false));
    }
}
