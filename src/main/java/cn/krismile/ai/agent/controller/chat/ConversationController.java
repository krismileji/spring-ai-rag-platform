package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.structure.chat.conversation.ConversationIdGenerator;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

    /**
     * 生成会话ID
     *
     * @return 会话ID
     * @since 1.0.0
     */
    @Operation(summary = "生成会话 ID")
    @PostMapping("/generate")
    public Mono<VO<String>> generate() {
        return Mono.fromSupplier(conversationIdGenerator::generate).map(R::data);
    }

    /**
     * 验证会话ID
     *
     * @param conversationId 会话ID
     * @return 验证结果
     * @since 1.0.0
     */
    @Operation(summary = "验证会话 ID")
    @PutMapping("/verify")
    public Mono<VO<Boolean>> verify(@RequestBody String conversationId) {
        return Mono.fromSupplier(() -> conversationIdGenerator.verify(conversationId)).map(R::data);
    }
}
