package cn.krismile.ai.agent.model.enumeration;

import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FilePlatformEnum
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FilePlatformEnum implements BaseEnum<String> {

    /**
     * 本地
     */
    LOCAL("LOCAL", "enum.file_platform.local"),

    /**
     * 阿里云对象存储
     */
    ALIYUN_OSS("ALIYUN_OSS", "enum.file_platform.aliyun_oss");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

}
