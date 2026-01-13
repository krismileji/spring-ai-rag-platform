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

    @Operation(summary = "聊天模型列表")
    @GetMapping("/chat/list")
    public VO<List<ChatModelVO>> chatModelList(@ModelAttribute ChatModelRequest query) {
        return R.data(this.chatModelService.listChatModels(query));
    }
}
