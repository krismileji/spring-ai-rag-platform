package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.model.response.chat.ChatConversationVO;
import cn.krismile.ai.agent.model.response.chat.ChatMemoryVO;
import cn.krismile.ai.agent.service.memory.ChatMemoryService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ChatMemoryController
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/chat/history")
@Tag(name = "历史聊天接口", description = "历史聊天接口")
public class ChatMemoryController {

    @Resource
    private ChatMemoryService chatMemoryService;

    @Operation(summary = "查询会话")
    @GetMapping("/conversations")
    public VO<List<ChatConversationVO>> chat() {
        return R.data(this.chatMemoryService.listConversations());
    }

    @Operation(summary = "查询会话记录")
    @GetMapping("/memories")
    public VO<List<ChatMemoryVO>> memories(@RequestParam String conversationId) {
        return R.data(this.chatMemoryService.listMemories(conversationId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/conversation/{conversationId}")
    public VO<?> conversation(@PathVariable String conversationId) {
        return R.ok(() -> chatMemoryService.delConversation(conversationId));
    }
}
