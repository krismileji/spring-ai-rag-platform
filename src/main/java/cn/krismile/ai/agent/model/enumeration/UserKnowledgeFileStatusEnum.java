package cn.krismile.ai.agent.model.enumeration;

import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户知识库文件状态枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UserKnowledgeFileStatusEnum implements BaseEnum<String> {

    /**
     * 未保存
     */
    UNSAVED("UNSAVED", "enum.user_knowledge_file_status.unsaved"),

    /**
     * 已保存
     */
    SAVED("SAVED", "enum.user_knowledge_file_status.saved");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

}
