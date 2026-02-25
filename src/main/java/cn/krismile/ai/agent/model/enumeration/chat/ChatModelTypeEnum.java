package cn.krismile.ai.agent.model.enumeration.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天模型类型枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ChatModelTypeEnum implements BaseEnum<String> {

    /**
     * 聊天模型
     */
    CHAT("CHAT", "enum.chat_model_type.chat"),

    /**
     * 嵌入模型
     */
    EMBEDDING("EMBEDDING", "enum.chat_model_type.embedding");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ChatModelTypeEnum of(String value) {
        return BaseEnum.parse(value, ChatModelTypeEnum.class);
    }
}