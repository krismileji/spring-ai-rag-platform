package cn.krismile.ai.agent.model.enumeration;

import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典类型枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DictTypeEnum implements BaseEnum<String> {

    /**
     * 聊天平台
     */
    CHAT_PLATFORM("CHAT_PLATFORM", "聊天平台"),

    /**
     * 聊天模型
     */
    CHAT_MODEL("CHAT_MODEL", "聊天模型");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

}
