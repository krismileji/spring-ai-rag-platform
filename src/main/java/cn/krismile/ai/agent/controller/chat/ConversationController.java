package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.structure.chat.conversation.ConversationIdGenerator;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 会话
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/conversation")
@Tag(name = "会话", description = "会话相关接口")
public class ConversationController {

    private final ConversationIdGenerator conversationIdGenerator;

    public ConversationController(ConversationIdGenerator conversationIdGenerator) {
        this.conversationIdGenerator = conversationIdGenerator;
    }

    @Operation(summary = "生成会话 ID")
    @PostMapping("/generate")
    public VO<String> generate() {
        return R.data(conversationIdGenerator.generate());
    }

    @Operation(summary = "验证会话 ID")
    @PutMapping("/verify")
    public VO<Boolean> verify(@RequestBody String conversationId) {
        return R.data(conversationIdGenerator.verify(conversationId));
    }
}
