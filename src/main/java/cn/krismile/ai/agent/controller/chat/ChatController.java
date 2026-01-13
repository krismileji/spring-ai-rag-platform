package cn.krismile.ai.agent.controller.chat;

import cn.krismile.ai.agent.model.request.chat.ChatRequest;
import cn.krismile.ai.agent.model.response.chat.ChatResponse;
import cn.krismile.ai.agent.structure.SchedulerDelegate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * ChatController
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口", description = "聊天接口")
public class ChatController {

    @Operation(summary = "聊天接口")
    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chat(@RequestBody ChatRequest request) {
        return request.getPlatform().strategy()
                .chat(request)
                .subscribeOn(SchedulerDelegate.create(Schedulers.boundedElastic()));
    }
}
