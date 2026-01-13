package cn.krismile.ai.agent.model.request.chat;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * 聊天模型配置选项VO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Schema(description = "聊天模型配置选项VO")
public class ChatOptionsRequest {

    @Schema(description = "请求超时时间，单位为秒")
    private Integer timeout;

    @Schema(description = "控制模型的随机性，值越大，模型的随机性越大")
    private Double temperature;

    @Schema(description = "控制模型在生成文本时是否引用和使用互联网搜索结果")
    private Boolean enableSearch;

    @Schema(description = "是否启用模型的思维过程")
    private Boolean enableThinking;

    public ChatOptions toChatOptions(ChatPlatformEnum platform) {
        PlatformChatOptions options = PlatformChatOptions.builder()
                .temperature(temperature)
                .enableSearch(enableSearch)
                .enableThinking(enableThinking)
                .build();
        return switch (platform) {
            case OLLAMA -> options.getOllamaOptions();
            case DEEPSEEK -> options.getDeepSeekOptions();
            case ALIYUN -> options.getDashScopeOptions();
        };
    }
}
