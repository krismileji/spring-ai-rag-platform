package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.model.request.model.ChatModelRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.structure.chat.chatmodel.ChatModelService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 聊天模型控制器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/model")
@Tag(name = "聊天模型", description = "聊天模型")
public class ChatModelController {

    @Resource
    private ChatModelService chatModelService;

    /**
     * 查询聊天模型列表
     *
     * @param query 查询参数
     * @return 聊天模型列表
     * @since 1.0.0
     */
    @Operation(summary = "聊天模型列表")
    @GetMapping("/chat/list")
    public Mono<VO<List<ChatModelVO>>> chatModelList(@ModelAttribute ChatModelRequest query) {
        return this.chatModelService.listChatModels(query).collectList() .map(R::data);
    }
}
