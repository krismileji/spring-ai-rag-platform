package cn.krismile.ai.agent.structure.chat.memory;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

import java.util.List;

/**
 * MessageWindowChatMemory
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Builder
public class MessageWindowChatMemory implements ChatMemory {

    public static final String MODEL = "model";

    private final ChatMemoryRepository chatMemoryRepository;

    private MessageWindowChatMemory(ChatMemoryRepository chatMemoryRepository) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @Override
    public void add(@NonNull String conversationId, @NonNull List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        this.chatMemoryRepository.saveAll(conversationId, messages);
    }

    @Override
    public @NonNull List<Message> get(@NonNull String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        return this.chatMemoryRepository.findByConversationId(conversationId);
    }

    @Override
    public void clear(@NonNull String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        this.chatMemoryRepository.deleteByConversationId(conversationId);
    }
}
