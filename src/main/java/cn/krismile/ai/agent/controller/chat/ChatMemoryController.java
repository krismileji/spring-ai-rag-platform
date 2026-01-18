package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.model.response.chat.ChatConversationVO;
import cn.krismile.ai.agent.model.response.chat.ChatMemoryVO;
import cn.krismile.ai.agent.structure.chat.memory.service.ChatMemoryService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

    /**
     * 查询会话列表
     *
     * @return 会话列表
     * @since 1.0.0
     */
    @Operation(summary = "查询会话")
    @GetMapping("/conversations")
    public Mono<VO<List<ChatConversationVO>>> chat() {
        return chatMemoryService.listConversations().collectList().map(R::data);
    }

    /**
     * 查询会话记录
     *
     * @param conversationId 会话ID
     * @return 会话记录列表
     * @since 1.0.0
     */
    @Operation(summary = "查询会话记录")
    @GetMapping("/memories")
    public Mono<VO<List<ChatMemoryVO>>> memories(@RequestParam String conversationId) {
        return chatMemoryService.listMemories(conversationId).collectList().map(R::data);
    }

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @return 是否删除成功
     * @since 1.0.0
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/conversation/{conversationId}")
    public Mono<VO<Boolean>> conversation(@PathVariable String conversationId) {
        return chatMemoryService.delConversation(conversationId).thenReturn(R.data(true));
    }
}
