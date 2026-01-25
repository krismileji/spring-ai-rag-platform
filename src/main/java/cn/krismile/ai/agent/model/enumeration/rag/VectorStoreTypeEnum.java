package cn.krismile.ai.agent.model.enumeration.rag;

import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 向量存储类型枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum VectorStoreTypeEnum implements BaseEnum<String> {

    /**
     * Qdrant
     */
    QDRANT("QDRANT", "Qdrant 向量存储");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;
    
}
